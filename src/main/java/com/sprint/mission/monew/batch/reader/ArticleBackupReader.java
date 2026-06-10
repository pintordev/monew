package com.sprint.mission.monew.batch.reader;

import com.sprint.mission.monew.domain.article.entity.Article;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
public class ArticleBackupReader implements ItemReader<Article> {

  private final EntityManagerFactory entityManagerFactory;

  @Value("${batch.article-backup.chunk-size}")
  private int chunkSize;

  private JpaPagingItemReader<Article> delegate;

  @Override
  public Article read() throws Exception {
    if (delegate == null) {
      delegate = buildDelegate();
    }
    return delegate.read();
  }

  private JpaPagingItemReader<Article> buildDelegate() throws Exception {
    LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
    Instant from = yesterday.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant to = yesterday.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    JpaPagingItemReader<Article> r = new JpaPagingItemReader<>();
    r.setEntityManagerFactory(entityManagerFactory);
    r.setQueryString(
        "SELECT a FROM Article a"
            + " WHERE a.createdAt >= :from AND a.createdAt < :to"
            + " AND a.deletedAt IS NULL"
            + " ORDER BY a.createdAt ASC, a.id ASC");
    r.setParameterValues(Map.of("from", from, "to", to));
    r.setPageSize(chunkSize);
    r.setSaveState(false);
    r.afterPropertiesSet();
    r.open(new org.springframework.batch.item.ExecutionContext());

    log.info("ArticleBackupReader 초기화: date={}, chunkSize={}", yesterday, chunkSize);
    return r;
  }
}