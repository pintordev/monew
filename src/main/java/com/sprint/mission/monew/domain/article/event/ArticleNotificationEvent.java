package com.sprint.mission.monew.domain.article.event;

import java.util.List;
import java.util.UUID;

public record ArticleNotificationEvent(
    UUID interestId,
    String message,
    List<UUID> subscriberIds) {}