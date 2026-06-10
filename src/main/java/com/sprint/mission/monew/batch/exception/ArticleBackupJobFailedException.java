package com.sprint.mission.monew.batch.exception;

public class ArticleBackupJobFailedException extends BatchException {

  private ArticleBackupJobFailedException(Throwable cause) {
    super(BatchErrorCode.ARTICLE_BACKUP_JOB_FAILED, null, cause);
  }

  public static ArticleBackupJobFailedException wrap(Throwable cause) {
    return new ArticleBackupJobFailedException(cause);
  }
}
