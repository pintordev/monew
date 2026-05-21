package com.sprint.mission.monew.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(GlobalExceptionHandlerTest.FakeController.class)
class GlobalExceptionHandlerTest {

  @Autowired MockMvc mockMvc;

  @RestController
  @RequestMapping("/test")
  static class FakeController {

    @GetMapping("/ok")
    void ok() {}

    @PostMapping("/body")
    void body(@RequestBody String payload) {}
  }

  @Nested
  @DisplayName("404 — 경로 없음")
  class NoResourceFound {

    @Test
    @DisplayName("존재하지 않는 경로 요청 시 404 반환")
    void 존재하지_않는_경로_404_반환() throws Exception {
      // given & when & then
      mockMvc
          .perform(get("/not-exist"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status").value(404))
          .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
          .andExpect(jsonPath("$.exceptionType").value("NoResourceFoundException"));
    }
  }

  @Nested
  @DisplayName("405 — HTTP 메서드 미지원")
  class MethodNotAllowed {

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드로 요청 시 405 반환")
    void 지원하지_않는_HTTP_메서드_405_반환() throws Exception {
      // given & when & then
      mockMvc
          .perform(delete("/test/ok"))
          .andExpect(status().isMethodNotAllowed())
          .andExpect(jsonPath("$.status").value(405))
          .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
          .andExpect(jsonPath("$.exceptionType").value("HttpRequestMethodNotSupportedException"));
    }
  }

  @Nested
  @DisplayName("400 — 본문 파싱 실패")
  class MessageNotReadable {

    @Test
    @DisplayName("잘못된 JSON 본문 전달 시 400 반환")
    void 잘못된_JSON_본문_400_반환() throws Exception {
      // given & when & then
      mockMvc
          .perform(post("/test/body")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{ invalid json }"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.code").value("MESSAGE_NOT_READABLE"))
          .andExpect(jsonPath("$.exceptionType").value("HttpMessageNotReadableException"));
    }
  }
}
