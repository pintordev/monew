package com.sprint.mission.monew.domain.interest.repository.dto;

import java.util.UUID;

public interface InterestArticleCount {

  UUID getInterestId();

  String getInterestName();

  Long getArticleCount();
}