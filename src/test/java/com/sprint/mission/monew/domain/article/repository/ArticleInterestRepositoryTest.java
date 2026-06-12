package com.sprint.mission.monew.domain.article.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.monew.common.config.JpaConfig;
import com.sprint.mission.monew.common.config.QuerydslConfig;
import com.sprint.mission.monew.domain.article.entity.Article;
import com.sprint.mission.monew.domain.article.entity.ArticleInterest;
import com.sprint.mission.monew.domain.article.entity.ArticleSource;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import com.sprint.mission.monew.domain.interest.repository.dto.InterestArticleCount;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class ArticleInterestRepositoryTest {

  @Autowired ArticleInterestRepository articleInterestRepository;
  @Autowired ArticleRepository articleRepository;
  @Autowired InterestRepository interestRepository;
  @Autowired EntityManager em;

  @BeforeEach
  void setUp() {
    articleInterestRepository.deleteAll();
    articleRepository.deleteAll();
    interestRepository.deleteAll();
    em.flush();
    em.clear();
  }

  @Nested
  @DisplayName("countByInterestSince")
  class CountByInterestSince {

    @Test
    @DisplayName("since 이후 생성된 기사를 관심사별로 집계한다")
    void since_이후_기사를_관심사별로_집계한다() {
      // given
      Instant since = Instant.now().minusSeconds(5);

      Interest interestA = interestRepository.save(Interest.create("인공지능", List.of("AI")));
      Interest interestB = interestRepository.save(Interest.create("경제", List.of("경제")));

      Article a1 = articleRepository.save(
          Article.create(ArticleSource.NAVER, "https://ex.com/1", "AI 기사1", Instant.now(), "요약1"));
      Article a2 = articleRepository.save(
          Article.create(ArticleSource.NAVER, "https://ex.com/2", "AI 기사2", Instant.now(), "요약2"));
      Article a3 = articleRepository.save(
          Article.create(ArticleSource.NAVER, "https://ex.com/3", "경제 기사", Instant.now(), "요약3"));

      articleInterestRepository.save(ArticleInterest.create(a1, interestA));
      articleInterestRepository.save(ArticleInterest.create(a2, interestA));
      articleInterestRepository.save(ArticleInterest.create(a3, interestB));

      em.flush();
      em.clear();

      // when
      List<InterestArticleCount> result = articleInterestRepository.countByInterestSince(since);

      // then
      Map<UUID, Long> countMap = result.stream()
          .collect(Collectors.toMap(
              InterestArticleCount::getInterestId,
              InterestArticleCount::getArticleCount));
      assertThat(countMap).hasSize(2);
      assertThat(countMap.get(interestA.getId())).isEqualTo(2L);
      assertThat(countMap.get(interestB.getId())).isEqualTo(1L);
    }
  }
}