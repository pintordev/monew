package com.sprint.mission.monew.common.filter;

import com.sprint.mission.monew.common.exception.UnauthorizedException;
import com.sprint.mission.monew.domain.user.document.UserSession;
import com.sprint.mission.monew.domain.user.repository.UserSessionRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class AuthFilter implements Filter {

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  private record MethodPath(String method, String path) {}

  private static final List<MethodPath> EXCLUDED = List.of(
      new MethodPath("POST", "/api/users"),
      new MethodPath("POST", "/api/users/login"),
      new MethodPath("GET", "/api/users/verify"),
      new MethodPath("POST", "/api/users/password/reset"),
      new MethodPath("POST", "/api/users/unlock")
  );

  private final UserSessionRepository userSessionRepository;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Autowired
  public AuthFilter(UserSessionRepository userSessionRepository,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
    this.userSessionRepository = userSessionRepository;
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (isExcluded(request.getMethod(), request.getRequestURI())) {
      chain.doFilter(request, response);
      return;
    }

    try {
      String token = request.getHeader("Monew-Request-User-ID");
      if (token == null || token.isBlank()) {
        throw UnauthorizedException.of();
      }
      UUID sessionToken = UUID.fromString(token);
      UserSession session = userSessionRepository.findById(sessionToken)
          .orElseThrow(UnauthorizedException::of);
      chain.doFilter(new UserIdHeaderWrapper(request, session.getUserId()), response);
    } catch (UnauthorizedException e) {
      handlerExceptionResolver.resolveException(request, response, null, e);
    }
  }

  private boolean isExcluded(String method, String uri) {
    return EXCLUDED.stream()
        .anyMatch(e -> e.method().equalsIgnoreCase(method) && PATH_MATCHER.match(e.path(), uri));
  }

  private static class UserIdHeaderWrapper extends HttpServletRequestWrapper {

    private final String userId;

    UserIdHeaderWrapper(HttpServletRequest request, UUID userId) {
      super(request);
      this.userId = userId.toString();
    }

    @Override
    public String getHeader(String name) {
      if ("Monew-Request-User-ID".equalsIgnoreCase(name)) {
        return userId;
      }
      return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      if ("Monew-Request-User-ID".equalsIgnoreCase(name)) {
        return Collections.enumeration(List.of(userId));
      }
      return super.getHeaders(name);
    }
  }
}