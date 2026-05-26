package com.sprint.mission.monew.domain.interest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.monew.domain.interest.dto.InterestCreateRequest;
import com.sprint.mission.monew.domain.interest.service.InterestService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InterestController.class)
class InterestControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean InterestService interestService;

  @Nested
  @DisplayName("POST /api/interests — 관심사 등록")
  class CreateInterest {

    @Test
    @DisplayName("name이 blank이면 400을 반환한다")
    void name이_blank이면_400을_반환한다() throws Exception {
      // given
      InterestCreateRequest request = new InterestCreateRequest("", List.of("AI"));

      // when & then
      mockMvc.perform(post("/api/interests")
              .header("Monew-Request-User-ID", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }
}
