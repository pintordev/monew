package com.sprint.mission.monew.domain.interest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.entity.Subscription;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import com.sprint.mission.monew.domain.interest.repository.SubscriptionRepository;
import com.sprint.mission.monew.domain.user.entity.User;
import com.sprint.mission.monew.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubscriptionIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired InterestRepository interestRepository;
  @Autowired UserRepository userRepository;
  @Autowired SubscriptionRepository subscriptionRepository;

  Interest interest;
  User user;

  @BeforeEach
  void setUp() {
    subscriptionRepository.deleteAll();
    interestRepository.deleteAll();
    userRepository.deleteAll();

    interest = interestRepository.save(Interest.create("인공지능", List.of("AI", "머신러닝")));
    user = userRepository.save(User.create("test@test.com", "테스터", "password123!"));
  }

  @Nested
  @DisplayName("POST /api/interests/{interestId}/subscriptions — 관심사 구독")
  class Subscribe {

    @Test
    @DisplayName("존재하지 않는 관심사 구독 시 404를 반환한다")
    void 존재하지_않는_관심사_구독_시_404를_반환한다() throws Exception {
      // when & then
      mockMvc
          .perform(post("/api/interests/{interestId}/subscriptions", UUID.randomUUID())
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("INTEREST_NOT_FOUND"));
    }

    @Test
    @DisplayName("이미 구독 중인 경우 409를 반환한다")
    void 이미_구독_중인_경우_409를_반환한다() throws Exception {
      // given
      subscriptionRepository.save(Subscription.create(interest, user));

      // when & then
      mockMvc
          .perform(post("/api/interests/{interestId}/subscriptions", interest.getId())
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("SUBSCRIPTION_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("정상 구독 시 200과 SubscriptionResponse를 반환하고 subscriberCount가 증가한다")
    void 정상_구독_시_200과_SubscriptionResponse를_반환하고_subscriberCount가_증가한다() throws Exception {
      // when & then
      mockMvc
          .perform(post("/api/interests/{interestId}/subscriptions", interest.getId())
              .header("Monew-Request-User-ID", user.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.interestId").value(interest.getId().toString()))
          .andExpect(jsonPath("$.interestName").value("인공지능"))
          .andExpect(jsonPath("$.interestSubscriberCount").value(1));

      assertThat(subscriptionRepository.existsByInterestIdAndUserId(
          interest.getId(), user.getId())).isTrue();
      assertThat(interestRepository.findById(interest.getId())
          .get().getSubscriberCount()).isEqualTo(1L);
    }
  }
}
