package com.sprint.mission.monew.batch.reader;

import com.sprint.mission.monew.domain.article.entity.Article;
import com.sprint.mission.monew.domain.article.repository.ArticleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class ArticleBackupReader implements ItemReader<Article> {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final ArticleRepository articleRepository;

  @Value("${batch.article-backup.chunk-size}")
  private int chunkSize;

  private Instant from;
  private Instant to;
  private UUID lastId;

  private Iterator<Article> iterator;

  @Override
  public Article read() {
    if (from == null) {
      LocalDate yesterday = LocalDate.now(KST).minusDays(1);
      from = yesterday.atStartOfDay(KST).toInstant();
      to = yesterday.plusDays(1).atStartOfDay(KST).toInstant();
      lastId = new UUID(0L, 0L);

      log.info("ArticleBackupReader 시작: date={}, chunkSize={}", yesterday, chunkSize);
    }

    while (iterator == null || !iterator.hasNext()) {
      List<Article> items = articleRepository.findArticlesForBackup(
          from, to, lastId, PageRequest.of(0, chunkSize));

      if (items.isEmpty()) {
        return null;
      }

      iterator = items.iterator();
      log.info("ArticleBackupReader chunk load 완료: size={}", items.size());
    }

    Article item = iterator.next();
    lastId = item.getId();
    return item;
  }
}