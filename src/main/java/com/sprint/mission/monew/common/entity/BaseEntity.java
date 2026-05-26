package com.sprint.mission.monew.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  @Id
  @Column(columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id = UUID.randomUUID();

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
