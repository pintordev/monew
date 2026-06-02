package com.sprint.mission.monew.batch;

import com.sprint.mission.monew.common.exception.ErrorCode;
import com.sprint.mission.monew.common.exception.MonewException;
import java.util.Map;

public class LogBackupFailedException extends MonewException {

  private LogBackupFailedException(Map<String, Object> details) {
    super(ErrorCode.INTERNAL_ERROR, details);
  }

  public static LogBackupFailedException withKey(String s3Key, Throwable cause) {
    LogBackupFailedException ex = new LogBackupFailedException(Map.of("s3Key", s3Key));
    ex.initCause(cause);
    return ex;
  }
}