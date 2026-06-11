package com.sprint.mission.monew.domain.interest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.common.dto.SortDirection;
import com.sprint.mission.monew.domain.interest.dto.InterestCreateRequest;
import com.sprint.mission.monew.domain.interest.dto.InterestOrderBy;
import com.sprint.mission.monew.domain.interest.dto.InterestQueryCondition;
import com.sprint.mission.monew.domain.interest.dto.InterestResponse;
import com.sprint.mission.monew.domain.interest.dto.InterestUpdateRequest;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.exception.InterestAlreadyExistsException;
import com.sprint.mission.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.mission.monew.domain.interest.mapper.InterestMapper;
import com.sprint.mission.monew.domain.interest.repository.InterestRepository;
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
class InterestServiceTest {

  @InjectMocks
  InterestService interestService;

  @Mock
  InterestRepository interestRepository;

  @Mock
  InterestMapper interestMapper;

  @Nested
  @DisplayName("관심사 목록 조회")
  class FindAll {

    @Test
    @DisplayName("조건에 맞는 관심사 목록을 조회한다")
    void 조건에_맞는_관심사_목록을_조회한다() {
      // given
      UUID userId = UUID.randomUUID();
      InterestQueryCondition condition = new InterestQueryCondition(
          "", InterestOrderBy.NAME, SortDirection.DESC, null, null, null, 10);
      CursorPageResponse<InterestResponse> expected =
          CursorPageResponse.of(List.of(), null, null, null, false, 0, 0L);

      given(interestRepository.findInterests(condition, userId)).willReturn(expected);

      // when
      CursorPageResponse<InterestResponse> result = interestService.findAll(condition, userId);

      // then
      assertThat(result).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("관심사 등록")
  class Create {

    @Test
    @DisplayName("대소문자·공백 차이 있어도 정규화 후 동일 이름이면 차단한다")
    void 대소문자_공백_차이_있어도_정규화_후_동일_이름이면_차단한다() {
      // given
      given(interestRepository.existsByName("AI 뉴스")).willReturn(false);
      given(interestRepository.findTypoCandidates(anyInt(), anyInt()))
          .willReturn(List.of("ai뉴스"));

      // when & then
      assertThatThrownBy(() -> interestService.create(new InterestCreateRequest("AI 뉴스", List.of())))
          .isInstanceOf(InterestAlreadyExistsException.class);
    }

    @Test
    @DisplayName("자모 1개 차이 유사도 0.8 이상이면 차단한다")
    void 자모_1개_차이_유사도_0_8이상이면_차단() {
      // given — "반도쳬" 등록 시도, DB에 "반도체" 존재
      given(interestRepository.existsByName("반도쳬")).willReturn(false);
      given(interestRepository.findTypoCandidates(anyInt(), anyInt()))
          .willReturn(List.of("반도체"));

      // when & then
      assertThatThrownBy(() -> interestService.create(new InterestCreateRequest("반도쳬", List.of())))
          .isInstanceOf(InterestAlreadyExistsException.class);
    }

    @Test
    @DisplayName("유사도 0.8 미만이면 통과한다")
    void 유사도_0_8미만이면_통과() {
      // given — "환경" 등록 시도, DB에 "금융" 존재
      given(interestRepository.existsByName("환경")).willReturn(false);
      given(interestRepository.findTypoCandidates(anyInt(), anyInt()))
          .willReturn(List.of("금융"));
      given(interestRepository.save(any())).willReturn(mock(Interest.class));
      given(interestMapper.toResponse(any())).willReturn(mock(InterestResponse.class));

      // when & then
      assertThatCode(() -> interestService.create(new InterestCreateRequest("환경", List.of())))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("관심사 키워드 수정")
  class UpdateKeywords {

    private UUID interestId;
    private InterestUpdateRequest request;

    @BeforeEach
    void setUp() {
      interestId = UUID.randomUUID();
      request = new InterestUpdateRequest(List.of("자연어처리", "GPT"));
    }

    @Test
    @DisplayName("존재하지 않는 관심사 수정 시 InterestNotFoundException이 발생한다")
    void 존재하지_않는_관심사_수정_시_예외가_발생한다() {
      // given
      given(interestRepository.findById(interestId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> interestService.updateKeywords(interestId, request))
          .isInstanceOf(InterestNotFoundException.class);
    }

    @Test
    @DisplayName("유효한 관심사 키워드 수정 시 InterestResponse를 반환한다")
    void 유효한_관심사_키워드_수정_시_InterestResponse를_반환한다() {
      // given
      Interest interest = Interest.create("인공지능", 11, List.of("AI"));
      InterestResponse expected =
          new InterestResponse(interest.getId(), "인공지능", List.of("자연어처리", "GPT"), 0L, false);

      given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
      given(interestMapper.toResponse(interest)).willReturn(expected);

      // when
      InterestResponse result = interestService.updateKeywords(interestId, request);

      // then
      assertThat(result.keywords()).containsExactlyElementsOf(request.keywords());
    }
  }

  @Nested
  @DisplayName("관심사 물리 삭제")
  class HardDelete {

    private UUID interestId;

    @BeforeEach
    void setUp() {
      interestId = UUID.randomUUID();
    }

    @Test
    @DisplayName("존재하지 않는 관심사 삭제 시 InterestNotFoundException이 발생한다")
    void 존재하지_않는_관심사_삭제_시_InterestNotFoundException이_발생한다() {
      // given
      given(interestRepository.findById(interestId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> interestService.hardDelete(interestId))
          .isInstanceOf(InterestNotFoundException.class);
    }

    @Test
    @DisplayName("존재하는 관심사 삭제 시 interestRepository.delete()가 호출된다")
    void 존재하는_관심사_삭제_시_repository_delete가_호출된다() {
      // given
      Interest interest = Interest.create("인공지능", 11, List.of("AI"));
      given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

      // when
      interestService.hardDelete(interestId);

      // then
      then(interestRepository).should().delete(interest);
    }
  }
}