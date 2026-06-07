package com.sprint.mission.monew.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.monew.domain.user.repository.UserSessionRepository;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

  @Mock
  private UserSessionRepository userSessionRepository;

  @Mock
  private HandlerExceptionResolver handlerExceptionResolver;

  @InjectMocks
  private AuthFilter authFilter;

  private final MockHttpServletRequest request = new MockHttpServletRequest();
  private final MockHttpServletResponse response = new MockHttpServletResponse();
  private final MockFilterChain chain = new MockFilterChain();

  @Nested
  @DisplayName("doFilter")
  class DoFilter {

    @Test
    @DisplayName("Monew-Request-User-ID 헤더 없으면 401 — chain 미실행")
    void Monew_Request_User_ID_헤더_없으면_401_chain_미실행() throws Exception {
      // given — 헤더 없음

      // when
      authFilter.doFilter(request, response, chain);

      // then
      assertThat(chain.getRequest()).isNull();
      then(handlerExceptionResolver).should().resolveException(
          org.mockito.ArgumentMatchers.any(),
          org.mockito.ArgumentMatchers.any(),
          org.mockito.ArgumentMatchers.isNull(),
          org.mockito.ArgumentMatchers.any()
      );
    }

    @Test
    @DisplayName("세션 없음(만료 포함) → 401, chain 미실행")
    void 세션_없음_만료_포함_401_chain_미실행() throws Exception {
      // given
      UUID sessionToken = UUID.randomUUID();
      request.addHeader("Monew-Request-User-ID", sessionToken.toString());
      given(userSessionRepository.findById(sessionToken)).willReturn(Optional.empty());

      // when
      authFilter.doFilter(request, response, chain);

      // then
      assertThat(chain.getRequest()).isNull();
      then(handlerExceptionResolver).should().resolveException(
          org.mockito.ArgumentMatchers.any(),
          org.mockito.ArgumentMatchers.any(),
          org.mockito.ArgumentMatchers.isNull(),
          org.mockito.ArgumentMatchers.any()
      );
    }

    @Test
    @DisplayName("제외 경로(POST /api/users/login)는 헤더 없어도 chain 통과")
    void 제외_경로는_헤더_없어도_chain_통과() throws Exception {
      // given
      request.setMethod("POST");
      request.setRequestURI("/api/users/login");

      // when
      authFilter.doFilter(request, response, chain);

      // then
      assertThat(chain.getRequest()).isNotNull();
      then(handlerExceptionResolver).should(never()).resolveException(
          org.mockito.ArgumentMatchers.any(),
          org.mockito.ArgumentMatchers.any(),
          org.mockito.ArgumentMatchers.any(),
          org.mockito.ArgumentMatchers.any()
      );
    }

    @Test
    @DisplayName("성공 시 chain 실행 및 Monew-Request-User-ID 헤더가 userId로 교체")
    void 성공_시_chain_실행_및_헤더가_userId로_교체() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      com.sprint.mission.monew.domain.user.document.UserSession session =
          com.sprint.mission.monew.domain.user.document.UserSession.create(userId, "1.2.3.4", "fp", 30);
      request.addHeader("Monew-Request-User-ID", session.getId().toString());
      given(userSessionRepository.findById(session.getId())).willReturn(java.util.Optional.of(session));

      // when
      authFilter.doFilter(request, response, chain);

      // then
      assertThat(chain.getRequest()).isNotNull();
      jakarta.servlet.http.HttpServletRequest wrapped =
          (jakarta.servlet.http.HttpServletRequest) chain.getRequest();
      assertThat(wrapped.getHeader("Monew-Request-User-ID")).isEqualTo(userId.toString());
    }
  }
}
