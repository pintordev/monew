package com.sprint.mission.monew.common.filter;

import com.sprint.mission.monew.common.exception.UnauthorizedException;
import com.sprint.mission.monew.domain.user.repository.UserSessionRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class AuthFilter implements Filter {

  private final UserSessionRepository userSessionRepository;

  @Qualifier("handlerExceptionResolver")
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    try {
      String token = request.getHeader("Monew-Request-User-ID");
      if (token == null || token.isBlank()) {
        throw UnauthorizedException.of();
      }
      chain.doFilter(request, response);
    } catch (UnauthorizedException e) {
      handlerExceptionResolver.resolveException(request, response, null, e);
    }
  }
}
