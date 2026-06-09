package com.sprint.mission.monew.common.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Isolation;

// Spring Batch JobRepository 기본값은 ISOLATION_SERIALIZABLE이지만,
// log-upload와 news-collect가 매시 정각에 동시 실행되면서 PostgreSQL SSI 충돌 발생.
// Spring Batch는 version 컬럼 낙관적 락을 사용하므로 READ_COMMITTED로도 안전하다.
@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

  @Override
  protected Isolation getIsolationLevelForCreate() {
    return Isolation.READ_COMMITTED;
  }
}