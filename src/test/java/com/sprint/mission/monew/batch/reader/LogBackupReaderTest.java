package com.sprint.mission.monew.batch.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.monew.batch.dto.LogContent;
import java.util.Collections;
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
  }
}
