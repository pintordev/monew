package com.sprint.mission.monew.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogBackupService {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${log.dir:logs}")
  private String logDir;

  public void upload() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    Path logFile = Path.of(logDir, yesterday.toString(), "app.log");

    if (!Files.exists(logFile)) {
      log.warn("로그 파일 없음: {}", logFile);
      return;
    }
  }
}