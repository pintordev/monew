package com.sprint.mission.monew.batch.reader;

import com.sprint.mission.monew.batch.dto.LogContent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
public class LogBackupReader implements ItemReader<LogContent> {

  private final CloudWatchLogsClient cloudWatchLogsClient;

  @Value("${monew.log-group}")
  private String logGroup;

  private boolean read = false;

  @Override
  public LogContent read() {
    if (read) {
      return null;
    }
    read = true;

    LocalDate yesterday = LocalDate.now().minusDays(1);
    long startTime = yesterday.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    long endTime = yesterday.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - 1;

    List<String> lines = new ArrayList<>();
    String nextToken = null;

    do {
      FilterLogEventsRequest.Builder requestBuilder = FilterLogEventsRequest.builder()
          .logGroupName(logGroup)
          .startTime(startTime)
          .endTime(endTime);
      if (nextToken != null) {
        requestBuilder.nextToken(nextToken);
      }
      FilterLogEventsResponse response = cloudWatchLogsClient.filterLogEvents(requestBuilder.build());
      response.events().forEach(event -> lines.add(event.message()));
      nextToken = response.nextToken();
    } while (nextToken != null);

    if (lines.isEmpty()) {
      log.warn("CloudWatch에서 어제({}) 로그를 찾을 수 없음: {}", yesterday, logGroup);
      return null;
    }

    byte[] content = String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
    return new LogContent(yesterday, content);
  }
}