package com.sprint.mission.monew.domain.user.document;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "user_sessions")
public class UserSession {

  @Id
  private UUID id;

  public static UserSession create(UUID userId, String ip, String deviceFingerprint,
      int timeoutMinutes) {
    UserSession session = new UserSession();
    session.id = UUID.randomUUID();
    return session;
  }
}