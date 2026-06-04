package com.sprint.mission.monew.domain.article.dto;

import com.sprint.mission.monew.common.dto.SortDirection;
import com.sprint.mission.monew.domain.article.entity.ArticleSource;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArticleQueryCondition(
    String keyword,
    UUID interestId,
    List<ArticleSource> sourceIn,
    Instant publishDateFrom,
    Instant publishDateTo,
    @NotNull ArticleOrderBy orderBy,
    @NotNull SortDirection direction,
    String cursor,
    Instant after,
    @NotNull @Min(1) Integer limit
) {

  @AssertTrue(message = "cursor와 after는 함께 전달되어야 합니다")
  public boolean isCursorAndAfterConsistent() {
    return (cursor == null) == (after == null);
  }
}
