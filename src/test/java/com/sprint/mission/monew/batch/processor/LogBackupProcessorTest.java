package com.sprint.mission.monew.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.monew.batch.dto.LogContent;
import com.sprint.mission.monew.batch.dto.UploadPayload;
import com.sprint.mission.monew.batch.util.BatchGzipUtils;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogBackupProcessorTest {

  @InjectMocks
  private LogBackupProcessor processor;

  private LocalDate yesterday;

  @BeforeEach
  void setUp() {
    yesterday = LocalDate.now().minusDays(1);
  }

  @Nested
  @DisplayName("백업 로그 파일 변환하기")
  class Processor {

    @Test
    @DisplayName("compressedData 조회 시 내부 배열이 변하지 않고 보호된다")
    void compressedData_getter_오버라이딩_방어적_복사() {
      // given
      byte[] original = {1, 2, 3};
      UploadPayload payload = new UploadPayload(
          Path.of("test.log"),
          "s3-key",
          original
      );

      // when
      byte[] copy = payload.compressedData();

      // 원본, 복사 배열 첫번째 값을 1에서 99로 설정
      original[0] = 99;
      copy[0] = 99;

      // then
      assertThat(payload.compressedData()[0]).isEqualTo((byte) 1);
    }

    @Test
    @DisplayName("LogContent를 UploadPayload로 변환한다")
    void LogContent를_UploadPayload로_변환한다() throws Exception {
      // given
      byte[] lines = "log content".getBytes();
      LogContent item = new LogContent(yesterday, lines);

      // when
      UploadPayload result = processor.process(item);

      // then
      assertThat(result).isNotNull();
      assertThat(result.s3Key()).isEqualTo(
          "logs/" + yesterday.format(BatchGzipUtils.PATH_FORMATTER)
              + "/app-" + yesterday.format(BatchGzipUtils.FILE_FORMATTER) + ".log.gz"
      );
      assertThat(result.compressedData()).isNotEmpty();
    }
  }
}