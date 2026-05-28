package com.sprint.mission.monew.domain.interest.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.mission.monew.domain.interest.exception.SubscriptionAlreadyExistsException;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import com.sprint.mission.monew.domain.user.entity.User;
import com.sprint.mission.monew.domain.user.exception.UserNotFoundException;
import com.sprint.mission.monew.domain.interest.repository.SubscriptionRepository;
import com.sprint.mission.monew.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @InjectMocks
  SubscriptionService subscriptionService;

  @Mock
  InterestRepository interestRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  SubscriptionRepository subscriptionRepository;

  UUID interestId;
  UUID userId;

  @BeforeEach
  void setUp() {
    interestId = UUID.randomUUID();
    userId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("관심사 구독")
  class Subscribe {

    @Test
    @DisplayName("존재하지 않는 관심사 구독 시 InterestNotFoundException이 발생한다")
    void 존재하지_않는_관심사_구독_시_InterestNotFoundException이_발생한다() {
      // given
      given(interestRepository.findById(interestId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> subscriptionService.subscribe(interestId, userId))
          .isInstanceOf(InterestNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 구독 시 UserNotFoundException이 발생한다")
    void 존재하지_않는_사용자_구독_시_UserNotFoundException이_발생한다() {
      // given
      Interest interest = Interest.create("인공지능", List.of("AI"));
      given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> subscriptionService.subscribe(interestId, userId))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("이미 구독 중인 경우 SubscriptionAlreadyExistsException이 발생한다")
    void 이미_구독_중인_경우_SubscriptionAlreadyExistsException이_발생한다() {
      // given
      Interest interest = Interest.create("인공지능", List.of("AI"));
      User user = User.create("test@test.com", "테스터", "password123!");
      given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(subscriptionRepository.existsByInterestIdAndUserId(interestId, userId))
          .willReturn(true);

      // when & then
      assertThatThrownBy(() -> subscriptionService.subscribe(interestId, userId))
          .isInstanceOf(SubscriptionAlreadyExistsException.class);
    }
  }
}
