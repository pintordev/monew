package com.sprint.mission.monew.domain.interest.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.monew.domain.interest.dto.InterestRegisterRequest;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.exception.InterestAlreadyExistsException;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

  @InjectMocks InterestService interestService;
  @Mock InterestRepository interestRepository;

  @Nested
  @DisplayName("관심사 등록")
  class Register {

    @Test
    @DisplayName("80% 이상 유사한 이름이 존재하면 InterestAlreadyExistsException이 발생한다")
    void 유사한_이름이_존재하면_예외가_발생한다() {
      // given
      UUID requestUserId = UUID.randomUUID();
      InterestRegisterRequest request = new InterestRegisterRequest("인공지능", List.of("AI"));
      Interest existing = Interest.create("인공지능X", List.of("머신러닝")); // 유사도 80% (거리 1, maxLen 5)

      given(interestRepository.findAll()).willReturn(List.of(existing));

      // when & then
      assertThatThrownBy(() -> interestService.register(request, requestUserId))
          .isInstanceOf(InterestAlreadyExistsException.class);
    }
  }
}