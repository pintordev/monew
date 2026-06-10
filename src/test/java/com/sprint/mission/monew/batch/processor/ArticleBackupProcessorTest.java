package com.sprint.mission.monew.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.monew.batch.dto.ArticleBackupItem;
import com.sprint.mission.monew.domain.article.entity.Article;
import com.sprint.mission.monew.domain.article.entity.ArticleSource;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleBackupProcessorTest {

  @InjectMocks
  ArticleBackupProcessor processor;

  @Nested
  @DisplayName("Article → ArticleBackupItem 변환")
  class Process {

    @Test
    @DisplayName("Article의 모든 필드가 ArticleBackupItem에 올바르게 매핑된다")
    void 모든_필드가_올바르게_매핑된다() throws Exception {
      // given
      Instant publishDate = Instant.parse("2026-06-09T10:00:00Z");
      Instant createdAt = Instant.parse("2026-06-09T10:01:00Z");

      Article article = Article.create(
          ArticleSource.NAVER, "https://example.com/news/1", "테스트 기사", publishDate, "요약 내용");
      ReflectionTestUtils.setField(article, "createdAt", createdAt);

      // when
      ArticleBackupItem result = processor.process(article);

      // then
      assertThat(result.id()).isEqualTo(article.getId());
      assertThat(result.source()).isEqualTo(ArticleSource.NAVER);
      assertThat(result.sourceUrl()).isEqualTo("https://example.com/news/1");
      assertThat(result.title()).isEqualTo("테스트 기사");
      assertThat(result.publishDate()).isEqualTo(publishDate);
      assertThat(result.summary()).isEqualTo("요약 내용");
      assertThat(result.commentCount()).isZero();
      assertThat(result.viewCount()).isZero();
      assertThat(result.createdAt()).isEqualTo(createdAt);
    }
  }
}
