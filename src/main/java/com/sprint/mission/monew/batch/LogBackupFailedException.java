package com.sprint.mission.monew.batch;

import com.sprint.mission.monew.common.exception.MonewInternalException;

public class LogBackupFailedException extends MonewInternalException {

  private LogBackupFailedException(String s3Key, Throwable cause) {
    super("로그 파일 S3 업로드 실패: " + s3Key, cause);
  }

  public static LogBackupFailedException withKey(String s3Key, Throwable cause) {
    return new LogBackupFailedException(s3Key, cause);
  }
}