package com.sprint.mission.monew.domain.interest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.mission.monew.domain.interest.service.SubscriptionService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  SubscriptionService subscriptionService;

  @Nested
  @DisplayName("POST /api/interests/{interestId}/subscriptions — 관심사 구독")
  class Subscribe {

    @Test
    @DisplayName("Monew-Request-User-ID 헤더가 없으면 400을 반환한다")
    void Monew_Request_User_ID_헤더가_없으면_400을_반환한다() throws Exception {
      // when & then
      mockMvc
          .perform(post("/api/interests/{interestId}/subscriptions", UUID.randomUUID()))
          .andExpect(status().isBadRequest());
    }
  }
}