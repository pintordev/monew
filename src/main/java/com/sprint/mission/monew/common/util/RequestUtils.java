package com.sprint.mission.monew.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtils {

  private RequestUtils() {
  }

  public static String resolveClientIp(HttpServletRequest request) {
    String cf = request.getHeader("CF-Connecting-IP");
    if (cf != null && !cf.isBlank()) {
      return cf;
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}