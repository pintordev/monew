package com.sprint.mission.monew.domain.user.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserSessionTest {

  @Nested
  @DisplayName("정적 팩토리 메서드")
  class Create {

    @Test
    @DisplayName("sessionToken은 호출마다 다른 랜덤 UUID로 생성")
    void sessionToken은_호출마다_다른_랜덤_UUID로_생성() {
      // given
      UUID userId = UUID.randomUUID();

      // when
      UserSession s1 = UserSession.create(userId, "1.2.3.4", "fp-abc", 30);
      UserSession s2 = UserSession.create(userId, "1.2.3.4", "fp-abc", 30);

      // then
      assertThat(s1.getId()).isNotNull();
      assertThat(s1.getId()).isNotEqualTo(s2.getId());
    }

    @Test
    @DisplayName("userId, ip, deviceFingerprint가 그대로 저장")
    void userId_ip_deviceFingerprint가_그대로_저장() {
      // given
      UUID userId = UUID.randomUUID();

      // when
      UserSession session = UserSession.create(userId, "1.2.3.4", "fp-abc", 30);

      // then
      assertThat(session.getUserId()).isEqualTo(userId);
      assertThat(session.getIp()).isEqualTo("1.2.3.4");
      assertThat(session.getDeviceFingerprint()).isEqualTo("fp-abc");
    }
  }
}
