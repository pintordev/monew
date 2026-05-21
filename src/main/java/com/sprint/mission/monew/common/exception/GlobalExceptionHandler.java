package com.sprint.mission.monew.common.exception;

import com.sprint.mission.monew.common.response.ErrorResponse;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
    ErrorCode code = ErrorCode.RESOURCE_NOT_FOUND;
    log.warn("[{}] {}", code.name(), e.getMessage());
    return ResponseEntity
        .status(code.getStatus())
        .body(new ErrorResponse(
            Instant.now(),
            code.name(),
            code.getMessage(),
            null,
            e.getClass().getSimpleName(),
            code.getStatus().value()
        ));
  }
}