package com.sprint.mission.monew.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RequestUtilsTest {

  @Nested
  @DisplayName("isSameSubnet24")
  class IsSameSubnet24 {

    @Test
    @DisplayName("같은 /24 대역이면 true를 반환한다")
    void 같은_24_대역이면_true를_반환한다() {
      assertThat(RequestUtils.isSameSubnet24("1.2.3.4", "1.2.3.99")).isTrue();
    }

    @Test
    @DisplayName("다른 /24 대역이면 false를 반환한다")
    void 다른_24_대역이면_false를_반환한다() {
      assertThat(RequestUtils.isSameSubnet24("1.2.3.4", "1.2.4.4")).isFalse();
    }

    @Test
    @DisplayName("점이 없는 IP(IPv6 등)는 전체 문자열로 비교한다")
    void 점이_없는_IP는_전체_문자열로_비교한다() {
      assertThat(RequestUtils.isSameSubnet24("::1", "::1")).isTrue();
      assertThat(RequestUtils.isSameSubnet24("::1", "::2")).isFalse();
    }
  }

  @Nested
  @DisplayName("resolveClientIp")
  class ResolveClientIp {

    @Test
    @DisplayName("CF-Connecting-IP 헤더가 있으면 해당 IP를 반환한다")
    void CF_Connecting_IP_헤더가_있으면_해당_IP를_반환한다() {
      HttpServletRequest req = mock(HttpServletRequest.class);
      given(req.getHeader("CF-Connecting-IP")).willReturn("203.0.113.1");

      assertThat(RequestUtils.resolveClientIp(req)).isEqualTo("203.0.113.1");
    }

    @Test
    @DisplayName("CF-Connecting-IP가 blank이면 X-Forwarded-For를 사용한다")
    void CF_Connecting_IP가_blank이면_X_Forwarded_For를_사용한다() {
      HttpServletRequest req = mock(HttpServletRequest.class);
      given(req.getHeader("CF-Connecting-IP")).willReturn("  ");
      given(req.getHeader("X-Forwarded-For")).willReturn("10.0.0.1, 172.16.0.1");

      assertThat(RequestUtils.resolveClientIp(req)).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("X-Forwarded-For가 blank이면 remoteAddr을 반환한다")
    void X_Forwarded_For가_blank이면_remoteAddr을_반환한다() {
      HttpServletRequest req = mock(HttpServletRequest.class);
      given(req.getHeader("CF-Connecting-IP")).willReturn(null);
      given(req.getHeader("X-Forwarded-For")).willReturn("  ");
      given(req.getRemoteAddr()).willReturn("127.0.0.1");

      assertThat(RequestUtils.resolveClientIp(req)).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("CF-Connecting-IP, X-Forwarded-For 모두 없으면 remoteAddr을 반환한다")
    void 두_헤더_모두_없으면_remoteAddr을_반환한다() {
      HttpServletRequest req = mock(HttpServletRequest.class);
      given(req.getHeader("CF-Connecting-IP")).willReturn(null);
      given(req.getHeader("X-Forwarded-For")).willReturn(null);
      given(req.getRemoteAddr()).willReturn("192.168.0.1");

      assertThat(RequestUtils.resolveClientIp(req)).isEqualTo("192.168.0.1");
    }
  }
}