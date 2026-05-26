package com.sprint.mission.monew.domain.interest.dto;

import java.util.List;
import java.util.UUID;

public record InterestDto(
    UUID id, String name, List<String> keywords, long subscriberCount, boolean subscribedByMe) {}
