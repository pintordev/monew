package com.sprint.mission.monew.common.config;

import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

@Configuration
public class CloudWatchConfig {

  private final String region;
  private final String namespace;
  private final Duration step;

  public CloudWatchConfig(
      @Value("${cloud.aws.region.static}") String region,
      @Value("${management.cloudwatch.metrics.export.namespace}") String namespace,
      @Value("${management.cloudwatch.metrics.export.step}") Duration step) {
    this.region = region;
    this.namespace = namespace;
    this.step = step;
  }

  @Bean
  public CloudWatchLogsClient cloudWatchLogsClient() {
    return CloudWatchLogsClient.builder()
        .region(Region.of(region))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "management.cloudwatch.metrics.export.enabled", havingValue = "true")
  public CloudWatchAsyncClient cloudWatchAsyncClient() {
    return CloudWatchAsyncClient.builder()
        .region(Region.of(region))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "management.cloudwatch.metrics.export.enabled", havingValue = "true")
  public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchAsyncClient cloudWatchAsyncClient) {
    String ns = this.namespace;
    Duration st = this.step;
    io.micrometer.cloudwatch2.CloudWatchConfig config = new io.micrometer.cloudwatch2.CloudWatchConfig() {
      @Override
      public String get(String key) {
        return null;
      }

      @Override
      public String namespace() {
        return ns;
      }

      @Override
      public Duration step() {
        return st;
      }
    };
    return new CloudWatchMeterRegistry(config, Clock.SYSTEM, cloudWatchAsyncClient);
  }
}