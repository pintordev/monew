package com.sprint.mission.monew.domain.interest.dto;

import com.sprint.mission.monew.common.dto.SortDirection;
import java.time.Instant;

public record InterestQueryCondition(
    String keyword,
    InterestOrderBy orderBy,
    SortDirection direction,
    String cursor,
    Instant after,
    int limit
) {}