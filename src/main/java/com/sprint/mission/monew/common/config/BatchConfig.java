package com.sprint.mission.monew.common.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Isolation;

// Spring Batch JobRepository 기본값은 ISOLATION_SERIALIZABLE이지만,
// log-upload와 news-collect가 매시 정각에 동시 실행되면서 PostgreSQL SSI 충돌 발생.
// Spring Batch는 version 컬럼 낙관적 락을 사용하므로 READ_COMMITTED/REPEATABLE_READ도
// 환경·동시성 요구사항에 따라 사용 가능하다 (Spring Batch 공식 문서 참고).
// DefaultBatchConfiguration 상속 시 BatchAutoConfiguration이 백오프되므로,
// 배치 스키마는 schema-batch.sql로 별도 관리한다.
@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

  @Override
  protected Isolation getIsolationLevelForCreate() {
    return Isolation.READ_COMMITTED;
  }
}