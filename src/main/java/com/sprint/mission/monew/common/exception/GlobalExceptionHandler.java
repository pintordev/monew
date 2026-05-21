package com.sprint.mission.monew.common.exception;

import com.sprint.mission.monew.common.response.ErrorResponse;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.stream.Collectors;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    ErrorCode code = ErrorCode.METHOD_NOT_ALLOWED;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    ErrorCode code = ErrorCode.VALIDATION_ERROR;
    Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            fe -> fe.getField(),
            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"
        ));
    log.warn("[{}] {}", code.name(), details);
    return ResponseEntity
        .status(code.getStatus())
        .body(new ErrorResponse(
            Instant.now(),
            code.name(),
            code.getMessage(),
            details,
            e.getClass().getSimpleName(),
            code.getStatus().value()
        ));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    ErrorCode code = ErrorCode.TYPE_MISMATCH;
    Map<String, Object> details = Map.of(
        e.getName(),
        e.getRequiredType() != null ? e.getRequiredType().getSimpleName() + " 타입이어야 합니다" : "invalid type"
    );
    log.warn("[{}] {}", code.name(), details);
    return ResponseEntity
        .status(code.getStatus())
        .body(new ErrorResponse(
            Instant.now(),
            code.name(),
            code.getMessage(),
            details,
            e.getClass().getSimpleName(),
            code.getStatus().value()
        ));
  }

  @ExceptionHandler(MonewException.class)
  public ResponseEntity<ErrorResponse> handleMonewException(MonewException e) {
    ErrorCode code = e.getErrorCode();
    log.warn("[{}] {}", code.name(), e.getMessage());
    return ResponseEntity
        .status(code.getStatus())
        .body(new ErrorResponse(
            Instant.now(),
            code.name(),
            code.getMessage(),
            e.getDetails(),
            e.getClass().getSimpleName(),
            code.getStatus().value()
        ));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException e) {
    ErrorCode code = ErrorCode.MESSAGE_NOT_READABLE;
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    ErrorCode code = ErrorCode.INTERNAL_ERROR;
    log.error("[{}] cause: {}, message: {}", code.name(), e.getClass().getSimpleName(),
        e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
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