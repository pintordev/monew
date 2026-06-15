// R5(user-activities) 측정용 Mongo 시드 — PG 유저별 user_activities 문서를 적재한다.
//
// 왜 필요한가:
//   R5는 user{i}@load.test 로 로그인해 자기 userId로 /api/user-activities/{userId} 를 친다.
//   앱(#293)은 이걸 Mongo 단독 조회(findById)로 처리하는데, seed-data-medium.sql 은 PG users만
//   채우고 UserCreatedEvent 를 발행하지 않아 Mongo 문서가 없다 → 404(측정 무효).
//   이 스크립트가 빈 활동내역 문서를 미리 깔아 R5를 정상 측정 가능하게 한다.
//
// ⚠️ UUID _id 인코딩(가장 중요):
//   Spring Boot 3.5의 Mongo uuid-representation 기본값은 JAVA_LEGACY 다(앱은 미설정 → 기본 적용).
//   JAVA_LEGACY 는 UUID 16바이트를 앞 8바이트·뒤 8바이트로 나눠 각각 뒤집어 BSON Binary
//   subtype 3 으로 저장한다. mongosh 의 UUID() 는 subtype 4(표준 순서)라 그대로 넣으면
//   앱의 findById 와 바이너리가 달라 매칭되지 않는다 → 여전히 404. 그래서 아래에서
//   legacyUuidBin() 으로 직접 변환해 넣는다. (앱/운영 설정은 건드리지 않는다 — 측정 전용.)
//
// 입력:  perf/seed/user-seed-data.generated.js  (extract-ids.sh 가 생성, globalThis.SEED_USERS)
// 사용:  cd perf/seed && mongosh "mongodb://localhost:27017/monew" seed-mongo-medium.js
//        (perf/run.sh 가 측정 전 자동 호출. MONGODB_URI 는 perf/docker-compose.yml 의 로컬 mongo)
// 재실행: 멱등 — 같은 _id를 먼저 지우고 다시 넣는다.

// 입력(SEED_USERS) 확보. 두 가지 실행 방식 모두 지원:
//   (a) host mongosh:  cd perf/seed && mongosh "<uri>" seed-mongo-medium.js
//       → 같은 디렉토리의 생성 파일을 load() 한다.
//   (b) 컨테이너 mongosh(host에 mongosh 없을 때): 데이터+이 스크립트를 이어붙여 stdin 으로 전달
//       cat user-seed-data.generated.js seed-mongo-medium.js | docker compose exec -T mongo mongosh "<uri>"
//       → 이 경우 SEED_USERS 가 앞서 이미 정의돼 있으니 load() 는 건너뛴다.
if (typeof globalThis.SEED_USERS === 'undefined') {
  try {
    load('user-seed-data.generated.js');
  } catch (e) {
    throw new Error(
      'SEED_USERS 를 못 구함 — 먼저 perf/extract-ids.sh 실행, 그리고 (a) perf/seed 에서 실행하거나 '
      + '(b) 데이터 파일을 이어붙여 stdin 으로 넘기세요. (' + e + ')',
    );
  }
}
if (!Array.isArray(globalThis.SEED_USERS) || globalThis.SEED_USERS.length === 0) {
  throw new Error('SEED_USERS 가 비었습니다 — extract-ids.sh 의 user 추출(빈 DB?)을 확인하세요.');
}

// 표준 UUID 문자열 → JAVA_LEGACY BSON Binary(subtype 3).
// 앞 8바이트(0..7)와 뒤 8바이트(8..15)를 각각 역순으로 재배열한 뒤 base64 로 BinData(3) 생성.
function legacyUuidBin(uuidStr) {
  const hex = String(uuidStr).replace(/-/g, '').toLowerCase();
  if (hex.length !== 32 || /[^0-9a-f]/.test(hex)) {
    throw new Error('잘못된 UUID 형식: ' + uuidStr);
  }
  const order = [7, 6, 5, 4, 3, 2, 1, 0, 15, 14, 13, 12, 11, 10, 9, 8];
  let reordered = '';
  for (const i of order) reordered += hex.substr(i * 2, 2);
  return BinData(3, Buffer.from(reordered, 'hex').toString('base64'));
}

// UserActivity 문서 형태(domain/useractivity/document/UserActivity 와 일치):
//   _id(UUID), email, nickname, createdAt(Instant→Date), 빈 활동 배열 4종.
//   _class 는 생략한다 — findById 는 리포지토리 반환 타입으로 매핑하므로 불필요.
const docs = SEED_USERS.map((u) => ({
  _id: legacyUuidBin(u.id),
  email: u.email,
  nickname: u.nickname,
  createdAt: new Date(u.createdAtMs),
  subscriptions: [],
  comments: [],
  commentLikes: [],
  articleViews: [],
}));

const ids = docs.map((d) => d._id);
const removed = db.user_activities.deleteMany({ _id: { $in: ids } }).deletedCount;
const res = db.user_activities.insertMany(docs, { ordered: false });
const inserted = res.insertedCount != null ? res.insertedCount : Object.keys(res.insertedIds).length;

print('[seed-mongo] user_activities 적재 완료: 삭제 ' + removed + ' / 삽입 ' + inserted);
print('[seed-mongo] 표본 _id(legacy bin): ' + (docs[0] ? docs[0]._id : 'n/a'));
print('[seed-mongo] R5 측정 준비됨 — 같은 DB에 앱(prod 프로파일)을 붙여 user-activities 를 조회.');