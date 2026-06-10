package com.sprint.mission.monew.batch.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.monew.domain.article.entity.Article;
import com.sprint.mission.monew.domain.article.entity.ArticleSource;
import com.sprint.mission.monew.domain.article.repository.ArticleRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleBackupReaderTest {

  @InjectMocks
  ArticleBackupReader reader;

  @Mock
  ArticleRepository articleRepository;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(reader, "chunkSize", 100);
  }

  private Article stubArticle() {
    return Article.create(
        ArticleSource.NAVER, "https://example.com/" + System.nanoTime(),
        "제목", Instant.now(), "요약");
  }

  @Nested
  @DisplayName("기사 백업 목록 읽기")
  class Read {

    @Test
    @DisplayName("대상 기사가 없으면 null을 반환한다")
    void 대상_기사_없으면_null_반환() {
      // given
      given(articleRepository.findArticlesForBackup(any(), any(), any(), any()))
          .willReturn(List.of());

      // when
      Article result = reader.read();

      // then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("기사가 있으면 순차적으로 반환하고 끝나면 null을 반환한다")
    void 기사_순차_반환_후_null() {
      // given
      Article article1 = stubArticle();
      Article article2 = stubArticle();

      given(articleRepository.findArticlesForBackup(any(), any(), any(), any()))
          .willReturn(List.of(article1, article2), List.of());

      // when
      Article r1 = reader.read();
      Article r2 = reader.read();
      Article r3 = reader.read();

      // then
      assertThat(r1).isSameAs(article1);
      assertThat(r2).isSameAs(article2);
      assertThat(r3).isNull();
    }
  }
}