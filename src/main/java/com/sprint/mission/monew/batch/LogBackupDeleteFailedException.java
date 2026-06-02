package com.sprint.mission.monew.batch;

import com.sprint.mission.monew.common.exception.MonewInternalException;
import java.nio.file.Path;

public class LogBackupDeleteFailedException extends MonewInternalException {

  private LogBackupDeleteFailedException(Path logFile, Throwable cause) {
    super("로컬 로그 파일 삭제 실패: " + logFile, cause);
  }

  public static LogBackupDeleteFailedException withPath(Path logFile, Throwable cause) {
    return new LogBackupDeleteFailedException(logFile, cause);
  }
}
