package com.sprint.mission.monew.domain.interest.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InterestTest {

  private String name;
  private List<String> keywords;

  @BeforeEach
  void setUp() {
    name = "인공지능";
    keywords = List.of("AI", "머신러닝", "딥러닝");
  }

  @Nested
  @DisplayName("정적 팩토리 메서드")
  class Create {

    @Test
    @DisplayName("이름과 키워드를 전달하면 관심사가 정상 생성된다")
    void 이름과_키워드를_전달하면_관심사가_정상_생성된다() {
      // when
      Interest interest = Interest.create(name, keywords);

      // then
      assertThat(interest.getId()).isNotNull();
      assertThat(interest.getName()).isEqualTo(name);
      assertThat(interest.getKeywords()).containsExactlyElementsOf(keywords);
      assertThat(interest.getSubscriberCount()).isZero();
    }
  }
}
