package com.sprint.mission.monew.domain.interest.entity;

import com.sprint.mission.monew.common.entity.BaseUpdatableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest extends BaseUpdatableEntity {

  @Column(nullable = false, length = 50)
  private String name;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "interest_keywords", joinColumns = @JoinColumn(name = "interest_id"))
  @Column(name = "keyword", nullable = false)
  private List<String> keywords = new ArrayList<>();

  @Column(nullable = false)
  private long subscriberCount = 0;

  public static Interest create(String name, List<String> keywords) {
    Interest interest = new Interest();
    interest.name = name;
    interest.keywords = new ArrayList<>(keywords);
    return interest;
  }
}
