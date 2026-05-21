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
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    void body(@RequestBody BodyRequest payload) {}

    @GetMapping("/type-mismatch/{id}")
    void typeMismatch(@PathVariable UUID id) {}
  }

  record BodyRequest(String name) {}

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
  @DisplayName("400 — 타입 변환 실패")
  class TypeMismatch {

    @Test
    @DisplayName("UUID 경로 변수에 잘못된 값 전달 시 400 + details 반환")
    void UUID_경로_변수에_잘못된_값_400_반환() throws Exception {
      // given & when & then
      mockMvc
          .perform(get("/test/type-mismatch/not-a-uuid"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
          .andExpect(jsonPath("$.details").exists())
          .andExpect(jsonPath("$.exceptionType").value("MethodArgumentTypeMismatchException"));
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
