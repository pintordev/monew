package com.sprint.mission.monew.common.entity;

import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseSoftDeletableEntity extends BaseUpdatableEntity {

  private Instant deletedAt;

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void softDelete() {
    if (isDeleted()) return;
    this.deletedAt = Instant.now();
  }
}
