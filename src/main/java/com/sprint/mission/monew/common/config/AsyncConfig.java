package com.sprint.mission.monew.common.config;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  @Bean(name = "userActivityExecutor")
  public Executor userActivityExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("ua-async-");
    executor.setRejectedExecutionHandler(new CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }

  // 좋아요 알림의 AFTER_COMMIT 쓰기를 요청 스레드에서 분리한다.
  // 동기 실행 시 NotificationService.create(REQUIRES_NEW)가 2번째 커넥션을 점유해
  // like 부하(100VU)에서 Hikari 풀 고갈·p95 30s 붕괴를 유발(perf-followup-fixes#C).
  //
  // 거부 정책: CallerRunsPolicy 금지(스레드 덤프로 확인된 데드락 원인).
  //   풀 포화 시 CallerRuns는 알림을 "요청 스레드"에서 실행하는데, 그 시점은 좋아요 tx의
  //   afterCompletion이라 요청 스레드가 커넥션 #1을 쥔 채 NotificationService.create(REQUIRES_NEW)의
  //   커넥션 #2를 기다려 Hikari 풀 데드락(전 스레드 park, p99 30s)이 발생한다.
  //   알림은 best-effort이므로 포화 시 드롭(+경고 로그)하여 요청 스레드를 절대 막지 않는다.
  @Bean(name = "notificationExecutor")
  public Executor notificationExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("noti-async-");
    executor.setRejectedExecutionHandler((task, exec) ->
        log.warn("[notificationExecutor] 큐 포화 — 좋아요 알림 1건 드롭 | queue={}, active={}, poolSize={}",
            exec.getQueue().size(), exec.getActiveCount(), exec.getPoolSize()));
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) ->
        log.error("[userActivityExecutor] 비동기 처리 실패 | method={}, params={}",
            method.getName(), Arrays.toString(params), ex);
  }
}