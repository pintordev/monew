package com.sprint.mission.monew.batch.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.monew.domain.article.entity.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.persistence.EntityManagerFactory;

@ExtendWith(MockitoExtension.class)
class ArticleBackupReaderTest {

  @InjectMocks
  ArticleBackupReader reader;

  @Mock
  EntityManagerFactory entityManagerFactory;

  @Mock
  JpaPagingItemReader<Article> delegate;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(reader, "chunkSize", 100);
    ReflectionTestUtils.setField(reader, "delegate", delegate);
  }

  @Nested
  @DisplayName("기사 백업 목록 읽기")
  class Read {

    @Test
    @DisplayName("대상 기사가 없으면 null을 반환한다")
    void 대상_기사_없으면_null_반환() throws Exception {
      // given
      given(delegate.read()).willReturn(null);

      // when
      Article result = reader.read();

      // then
      assertThat(result).isNull();
    }
  }
}
