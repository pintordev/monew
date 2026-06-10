package com.sprint.mission.monew.batch.config;

import com.sprint.mission.monew.batch.reader.ArticleBackupReader;
import com.sprint.mission.monew.batch.writer.ArticleBackupWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ArticleBackupJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final ArticleBackupReader articleBackupReader;
  private final ArticleBackupWriter articleBackupWriter;

  @Value("${batch.article-backup.chunk-size}")
  private int chunkSize;

  @Bean(name = "articleBackupJob")
  public Job articleBackupJob() {
    throw new UnsupportedOperationException("미구현");
  }

  @Bean
  public Step articleBackupStep() {
    throw new UnsupportedOperationException("미구현");
  }
}