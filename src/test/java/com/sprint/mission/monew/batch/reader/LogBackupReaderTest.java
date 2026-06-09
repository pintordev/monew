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
    @DisplayName("CloudWatch에 어제 로그 이벤트가 없으면 null을 반환한다")
    void CloudWatch에_어제_로그_이벤트가_없으면_null을_반환한다() throws Exception {
      // given
      FilterLogEventsResponse emptyResponse = FilterLogEventsResponse.builder()
          .events(Collections.emptyList())
          .build();
      given(cloudWatchLogsClient.filterLogEvents(any(FilterLogEventsRequest.class)))
          .willReturn(emptyResponse);

      // when
      LogContent result = reader.read();

      // then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("read()를 두 번 호출하면 두 번째는 null을 반환한다")
    void read를_두_번_호출하면_두_번째는_null을_반환한다() throws Exception {
      // given
      FilterLogEventsResponse response = FilterLogEventsResponse.builder()
          .events(Collections.emptyList())
          .build();
      given(cloudWatchLogsClient.filterLogEvents(any(FilterLogEventsRequest.class)))
          .willReturn(response);

      // when
      reader.read();
      LogContent secondResult = reader.read();

      // then
      assertThat(secondResult).isNull();
    }

    @Test
    @DisplayName("CloudWatch 로그 이벤트를 수집해 LogContent를 반환한다")
    void CloudWatch_로그_이벤트를_수집해_LogContent를_반환한다() throws Exception {
      // given
      LocalDate yesterday = LocalDate.now().minusDays(1);
      FilterLogEventsResponse response = FilterLogEventsResponse.builder()
          .events(List.of(
              FilteredLogEvent.builder().message("line1").build(),
              FilteredLogEvent.builder().message("line2").build()
          ))
          .build();
      given(cloudWatchLogsClient.filterLogEvents(any(FilterLogEventsRequest.class)))
          .willReturn(response);

      // when
      LogContent result = reader.read();

      // then
      assertThat(result).isNotNull();
      assertThat(result.date()).isEqualTo(yesterday);
      assertThat(new String(result.lines(), StandardCharsets.UTF_8)).isEqualTo("line1\nline2");
    }

    @Test
    @DisplayName("여러 페이지에 걸친 로그를 nextToken으로 모두 수집한다")
    void 여러_페이지에_걸친_로그를_nextToken으로_모두_수집한다() throws Exception {
      // given
      LocalDate yesterday = LocalDate.now().minusDays(1);
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
      LogContent result = reader.read();

      // then
      assertThat(result).isNotNull();
      assertThat(result.date()).isEqualTo(yesterday);
      assertThat(new String(result.lines(), StandardCharsets.UTF_8)).isEqualTo("line1\nline2");
    }
  }
}
