package com.sprint.mission.monew.batch.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.monew.batch.dto.ArticleBackupItem;
import com.sprint.mission.monew.batch.metrics.ArticleBackupMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class ArticleBackupWriter implements ItemWriter<ArticleBackupItem> {

  private final S3Client s3Client;
  private final ObjectMapper objectMapper;
  private final ArticleBackupMetrics metrics;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Override
  public void write(Chunk<? extends ArticleBackupItem> chunk) throws Exception {
    throw new UnsupportedOperationException("미구현");
  }
}