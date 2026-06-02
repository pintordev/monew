package com.sprint.mission.monew.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogBackupService {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${monew.log-dir}")
  private String logDir;

  public void upload() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    Path logFile = Path.of(logDir, "monew." + yesterday + ".log");

    if (!Files.exists(logFile)) {
      log.warn("로그 파일 없음: {}", logFile);
      return;
    }

    String s3Key = "logs/" + yesterday + "/monew." + yesterday + ".log";
    try {
      s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(s3Key).build());
      log.info("이미 업로드됨, 건너뜀: {}", s3Key);
      return;
    } catch (NoSuchKeyException ignored) {
      // 업로드 진행
    }

    try {
      s3Client.putObject(
          PutObjectRequest.builder().bucket(bucket).key(s3Key).build(),
          RequestBody.fromFile(logFile));
      log.info("업로드 완료: {}", s3Key);
    } catch (Exception e) {
      throw LogBackupFailedException.withKey(s3Key, e);
    }
  }
}