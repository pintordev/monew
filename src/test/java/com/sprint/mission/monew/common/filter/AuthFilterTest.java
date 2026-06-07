package com.sprint.mission.monew.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.monew.domain.user.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
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

  @InjectMocks
  private AuthFilter authFilter;

  @Mock
  private UserSessionRepository userSessionRepository;

  @Mock
  private HandlerExceptionResolver handlerExceptionResolver;

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
  }
}
