package com.sprint.mission.monew.domain.interest.dto;

import com.sprint.mission.monew.common.dto.SortDirection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record InterestQueryCondition(
    String keyword,
    @NotNull InterestOrderBy orderBy,
    @NotNull SortDirection direction,
    String cursor,
    Instant after,
    @NotNull @Min(1) Integer limit
) {}