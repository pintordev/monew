package com.sprint.mission.monew.batch.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.monew.batch.dto.LogContent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;

@ExtendWith(MockitoExtension.class)
class LogBackupReaderTest {

  @Mock
  private CloudWatchLogsClient cloudWatchLogsClient;

  @InjectMocks
  private LogBackupReader reader;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(reader, "logGroup", "test-log-group");
  }

  @Nested
  @DisplayName("CloudWatch 로그 읽기")
  class Read {

    @Test
    @DisplayName("단일 페이지를 읽으면 pageNumber=1인 LogContent를 반환한다")
    void 단일_페이지를_읽으면_pageNumber_1인_LogContent를_반환한다() throws Exception {
      // given
      LocalDate yesterday = LocalDate.now().minusDays(1);
      FilterLogEventsResponse response = FilterLogEventsResponse.builder()
          .events(List.of(FilteredLogEvent.builder().message("line1").build()))
          .build();
      given(cloudWatchLogsClient.filterLogEvents(any(FilterLogEventsRequest.class)))
          .willReturn(response);

      // when
      LogContent result = reader.read();

      // then
      assertThat(result).isNotNull();
      assertThat(result.pageNumber()).isEqualTo(1);
      assertThat(result.date()).isEqualTo(yesterday);
      assertThat(new String(result.lines(), StandardCharsets.UTF_8)).isEqualTo("line1");
    }

    @Test
    @DisplayName("두 번째 read()는 nextToken으로 pageNumber=2를 반환한다")
    void 두_번째_read는_nextToken으로_pageNumber_2를_반환한다() throws Exception {
      // given
      FilterLogEventsResponse firstPage = FilterLogEventsResponse.builder()
          .events(List.of(FilteredLogEvent.builder().message("line1").build()))
          .nextToken("token123")
          .build();
      FilterLogEventsResponse secondPage = FilterLogEventsResponse.builder()
          .events(List.of(FilteredLogEvent.builder().message("line2").build()))
          .build();
      given(cloudWatchLogsClient.filterLogEvents(any(FilterLogEventsRequest.class)))
          .willReturn(firstPage, secondPage);

      // when
      reader.read();
      LogContent second = reader.read();

      // then
      assertThat(second).isNotNull();
      assertThat(second.pageNumber()).isEqualTo(2);
      assertThat(new String(second.lines(), StandardCharsets.UTF_8)).isEqualTo("line2");
    }
  }
}