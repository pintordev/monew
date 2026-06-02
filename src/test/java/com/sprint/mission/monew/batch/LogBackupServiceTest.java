package com.sprint.mission.monew.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class LogBackupServiceTest {

  @InjectMocks LogBackupService logBackupService;
  @Mock S3Client s3Client;

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(logBackupService, "logDir", tempDir.toString());
    ReflectionTestUtils.setField(logBackupService, "bucket", "test-bucket");
  }

  @Nested
  @DisplayName("로그 파일 S3 업로드")
  class Upload {

    @Test
    @DisplayName("전날 로그 파일이 없으면 S3 업로드를 호출하지 않는다")
    void 전날_로그_파일이_없으면_S3_업로드를_호출하지_않는다() {
      // given — tempDir에 로그 파일 없음

      // when
      logBackupService.upload();

      // then
      verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
  }
}