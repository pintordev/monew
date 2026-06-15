#!/usr/bin/env bash
#
# 시드된 DB에서 k6 측정용 ID 풀을 추출한다.
#
# 왜 필요한가:
#   - 요청에 **실재하는 id**를 써야 한다. 없는 id는 서버가 DB 조회도 없이 404 → "엄청 빠름"이라는 가짜 수치.
#   - **매번 다른 id**로 분산해야 한다. 같은 id만 반복하면 캐시 히트로 비현실적으로 빨라진다.
#   → 미리 풀로 뽑아두고 k6에서 무작위로 골라 쓴다.
#
# 산출 파일(.gitignore 대상 — 스크립트만 커밋, 산출물은 환경 의존이라 추적 안 함):
#   article_ids.csv                : articleId    (인기 기사 + 한산/일반 기사 혼합 → 변동성 관찰)
#   comment_ids.csv                : commentId,articleId
#   seed/user-seed-data.generated.js : globalThis.SEED_USERS=[...]  (R5 Mongo 시드 입력)
# (k6 인증용 userId는 setup() 로그인으로 얻는다. 여기 유저 추출은 R5 Mongo 적재 전용이다.)
#
# 사용:
#   docker compose -f perf/docker-compose.yml up -d postgres   # 시드 먼저(seed-data-medium.sql)
#   perf/extract-ids.sh                                         # 기본 DB·기본 표본수
#   LIMIT=1000 OUT_DIR=perf/speed perf/extract-ids.sh          # 표본수·출력 위치 변경
#
set -euo pipefail

# 기본값: perf/docker-compose.yml 의 postgres. 필요시 환경변수로 덮어쓴다.
DB_URL="${DB_URL:-postgresql://monew:monew@localhost:5433/monew}"  # 5433 = perf/docker-compose.yml의 host 매핑(5432 충돌 회피)
LIMIT="${LIMIT:-500}"                                   # 풀 크기(엔드포인트별 무작위 추출 대상)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="${OUT_DIR:-$SCRIPT_DIR}"                       # 기본: 이 스크립트와 같은 perf/ 디렉토리

command -v psql >/dev/null 2>&1 || { echo "psql 이 필요합니다 (PostgreSQL client)." >&2; exit 1; }
[[ "$LIMIT" =~ ^[0-9]+$ ]] || { echo "LIMIT 은 정수여야 합니다: $LIMIT" >&2; exit 1; }
mkdir -p "$OUT_DIR"

# 공통: 튜플만(-t) · 정렬 안 함(-A) · 콤마 구분(-F','). 빈 줄은 k6 쪽에서 걸러진다.
psql_csv() { psql "$DB_URL" -tA -F',' -c "$1"; }

# DB_URL의 user:password를 마스킹해 로그/CI 아티팩트에 자격증명이 남지 않게 한다.
REDACTED_DB_URL="$(sed -E 's#(://[^:/@]+):[^@]*@#\1:***@#' <<< "$DB_URL")"
echo "[extract] DB=$REDACTED_DB_URL  LIMIT=$LIMIT  OUT_DIR=$OUT_DIR"

# ── 기사: 인기(댓글 많은) + 일반/한산 혼합 ───────────────────
# 인기 기사만 뽑으면 깊은 커서·정렬 비용이 항상 크게 나오고, 한산 기사만 뽑으면 안 드러난다 → 섞는다.
HOT=$(( LIMIT / 10 ))                                   # 약 10%는 댓글 상위(인기) 기사
# 랜덤 집합에서 hot과 겹치는 id를 제외한 뒤 UNION ALL → 중복 제거로 행 수가 LIMIT보다 줄지 않게.
psql_csv "
  WITH hot AS (
    SELECT id FROM articles
    WHERE deleted_at IS NULL
    ORDER BY comment_count DESC
    LIMIT $HOT
  ),
  sampled AS (
    SELECT id FROM articles
    WHERE deleted_at IS NULL
      AND id NOT IN (SELECT id FROM hot)
    ORDER BY random()
    LIMIT $(( LIMIT - HOT ))
  )
  SELECT id FROM hot
  UNION ALL
  SELECT id FROM sampled
" > "$OUT_DIR/article_ids.csv"

# ── 댓글: commentId,articleId (R3 등 댓글 목록·좋아요 측정용) ──
psql_csv "
  SELECT c.id, c.article_id FROM comments c
  WHERE c.deleted_at IS NULL
  ORDER BY random() LIMIT $LIMIT
" > "$OUT_DIR/comment_ids.csv"

# ── 유저: R5(user-activities) Mongo 시드용 ───────────────────────
# R5는 user1~user{N}@load.test 로 로그인해 자기 userId로 활동내역을 조회한다(perf/speed/read.js).
# 그런데 seed-data-medium.sql은 PG users만 채우고 UserCreatedEvent를 발행하지 않아 Mongo
# user_activities 문서가 없다 → R5가 404(측정 무효). 그래서 PG의 실제 userId/프로필을 뽑아
# seed-mongo-medium.js 가 그대로 적재한다(_id는 PG UUID와 정확히 일치해야 findById가 맞는다).
#
# 산출: perf/seed/user-seed-data.generated.js — `globalThis.SEED_USERS=[...]` 형태의 JS.
#   mongosh가 load()로 읽을 수 있게 CSV가 아니라 JS 리터럴로 떨군다(파싱 의존성 0).
#   gitignore 대상(환경 의존·재생성 가능). 전 유저를 뽑아 LOGIN_USERS 값과 무관히 동작.
SEED_DATA_FILE="$SCRIPT_DIR/seed/user-seed-data.generated.js"
# void(...) 로 감싸 대입식이 값을 반환하지 않게 한다 — mongosh 를 stdin(REPL) 으로 쓸 때
# 대입 결과(1만 건 배열)가 화면에 메아리치는 것을 막는다(기능엔 무관, 출력만 깔끔).
psql "$DB_URL" -tAX -c "
  SELECT 'void(globalThis.SEED_USERS=' || COALESCE(
    json_agg(json_build_object(
      'id', id, 'email', email, 'nickname', nickname,
      'createdAtMs', (extract(epoch FROM created_at) * 1000)::bigint
    ) ORDER BY email)::text, '[]') || ');'
  FROM users WHERE deleted_at IS NULL
" > "$SEED_DATA_FILE"

# ── 결과 요약 ─────────────────────────────────────────────────
echo "[extract] 완료:"
for f in article_ids comment_ids; do
  printf '  %-16s %s 행\n' "$f.csv" "$(grep -c . "$OUT_DIR/$f.csv" || true)"
done
printf '  %-16s %s 유저\n' "user-seed-data" "$(grep -o '"id"' "$SEED_DATA_FILE" | wc -l | tr -d ' ')"
echo "[extract] k6 스크립트가 이 csv들을 open() 한다. 시드를 다시 채웠으면 이 스크립트도 다시 실행할 것."
echo "[extract] R5 측정 전: mongosh \"\$MONGODB_URI\" perf/seed/seed-mongo-medium.js 로 Mongo 적재."