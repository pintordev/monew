package com.sprint.mission.monew.domain.interest.exception;

import com.sprint.mission.monew.common.exception.ErrorCode;
import java.util.Map;

public class InterestAlreadyExistsException extends InterestException {

  private InterestAlreadyExistsException(Map<String, Object> details) {
    super(ErrorCode.INTEREST_ALREADY_EXISTS, details);
  }

  public static InterestAlreadyExistsException withName(String name) {
    return new InterestAlreadyExistsException(Map.of("name", name));
  }
}
