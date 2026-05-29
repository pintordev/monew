package com.sprint.mission.monew.domain.interest.repository.querydsl.impl;

import static com.sprint.mission.monew.domain.interest.entity.QInterest.interest;
import static com.sprint.mission.monew.domain.interest.entity.QInterestKeyword.interestKeyword;
import static com.sprint.mission.monew.domain.interest.entity.QSubscription.subscription;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.monew.common.dto.CursorPageResponse;
import com.sprint.mission.monew.common.dto.SortDirection;
import com.sprint.mission.monew.domain.interest.dto.InterestOrderBy;
import com.sprint.mission.monew.domain.interest.dto.InterestQueryCondition;
import com.sprint.mission.monew.domain.interest.dto.InterestResponse;
import com.sprint.mission.monew.domain.interest.entity.Interest;
import com.sprint.mission.monew.domain.interest.entity.InterestKeyword;
import com.sprint.mission.monew.domain.interest.entity.QInterestKeyword;
import com.sprint.mission.monew.domain.interest.repository.querydsl.InterestCustomRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class InterestCustomRepositoryImpl implements InterestCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<InterestResponse> findInterests(InterestQueryCondition condition,
      UUID userId) {
    List<Tuple> raw = queryFactory
        .selectDistinct(interest, subscription.id)
        .from(interest)
        .leftJoin(interest.keywords, interestKeyword).fetchJoin()
        .leftJoin(subscription).on(
            subscription.interest.eq(interest).and(subscription.user.id.eq(userId)))
        .where(
            likeNameOrKeyword(condition.keyword()),
            cursorCondition(condition)
        )
        .orderBy(
            buildOrderSpecifier(condition.orderBy(), condition.direction()),
            buildCreatedAtOrderSpecifier(condition.direction())
        )
        .limit(condition.limit() + 1L)
        .fetch();

    boolean hasNext = raw.size() > condition.limit();
    List<Tuple> content = hasNext ? raw.subList(0, condition.limit()) : raw;

    List<InterestResponse> responses = content.stream()
        .map(t -> toResponse(t.get(interest), t.get(subscription.id) != null))
        .toList();

    String nextCursor = hasNext
        ? extractCursor(content.get(content.size() - 1).get(interest), condition.orderBy())
        : null;
    Instant nextAfter = hasNext
        ? content.get(content.size() - 1).get(interest).getCreatedAt()
        : null;

    Long total = queryFactory
        .select(interest.count())
        .from(interest)
        .where(likeNameOrKeyword(condition.keyword()))
        .fetchOne();

    return CursorPageResponse.of(
        responses,
        nextCursor,
        nextAfter,
        hasNext,
        content.size(),
        total
    );
  }

  private BooleanExpression likeNameOrKeyword(String keyword) {
    return !StringUtils.hasText(keyword) ? null
        : likeName(keyword).or(likeKeyword(keyword));
  }

  private BooleanExpression likeName(String keyword) {
    return interest.name.containsIgnoreCase(keyword);
  }

  private BooleanExpression likeKeyword(String keyword) {
    QInterestKeyword kwdSub = new QInterestKeyword("kwdSub");
    return JPAExpressions.selectOne()
        .from(kwdSub)
        .where(
            kwdSub.interest.eq(interest),
            kwdSub.keyword.containsIgnoreCase(keyword)
        )
        .exists();
  }

  private BooleanExpression cursorCondition(InterestQueryCondition condition) {
    String cursor = condition.cursor();
    Instant after = condition.after();
    boolean isAsc = condition.direction() == SortDirection.ASC;
    if (cursor == null) {
      return null;
    }
    return switch (condition.orderBy()) {
      case NAME -> buildCursorExpression(interest.name, cursor, after, isAsc);
      case SUBSCRIBER_COUNT ->
          buildCursorExpression(interest.subscriberCount, Long.parseLong(cursor), after, isAsc);
    };
  }

  private BooleanExpression buildCursorExpression(
      ComparableExpression<String> field, String cursorValue, Instant after, boolean isAsc) {
    return isAsc
        ? field.gt(cursorValue).or(field.eq(cursorValue).and(interest.createdAt.gt(after)))
        : field.lt(cursorValue).or(field.eq(cursorValue).and(interest.createdAt.lt(after)));
  }

  private BooleanExpression buildCursorExpression(
      NumberExpression<Long> field, Long cursorValue, Instant after, boolean isAsc) {
    return isAsc
        ? field.gt(cursorValue).or(field.eq(cursorValue).and(interest.createdAt.gt(after)))
        : field.lt(cursorValue).or(field.eq(cursorValue).and(interest.createdAt.lt(after)));
  }

  private OrderSpecifier<?> buildOrderSpecifier(InterestOrderBy orderBy, SortDirection direction) {
    Order dir = direction == SortDirection.ASC ? Order.ASC : Order.DESC;
    return switch (orderBy) {
      case NAME -> new OrderSpecifier<>(dir, interest.name);
      case SUBSCRIBER_COUNT -> new OrderSpecifier<>(dir, interest.subscriberCount);
    };
  }

  private OrderSpecifier<?> buildCreatedAtOrderSpecifier(SortDirection direction) {
    Order dir = direction == SortDirection.ASC ? Order.ASC : Order.DESC;
    return new OrderSpecifier<>(dir, interest.createdAt);
  }

  private String extractCursor(Interest i, InterestOrderBy orderBy) {
    return switch (orderBy) {
      case NAME -> i.getName();
      case SUBSCRIBER_COUNT -> String.valueOf(i.getSubscriberCount());
    };
  }

  private InterestResponse toResponse(Interest i, boolean subscribedByMe) {
    return new InterestResponse(
        i.getId(),
        i.getName(),
        i.getKeywords().stream().map(InterestKeyword::getKeyword).toList(),
        i.getSubscriberCount(),
        subscribedByMe
    );
  }
}
