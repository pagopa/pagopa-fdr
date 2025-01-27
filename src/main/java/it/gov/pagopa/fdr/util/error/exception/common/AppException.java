package it.gov.pagopa.fdr.util.error.exception.common;

import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

  private final transient AppErrorCodeMessageEnum codeMessage;

  private final transient Object[] args;

  public AppException(Throwable cause, AppErrorCodeMessageEnum codeMessage) {
    super(cause);
    this.codeMessage = codeMessage;
    this.args = null;
  }

  public AppException(Throwable cause, AppErrorCodeMessageEnum codeMessage, Object... args) {
    super(cause);
    this.codeMessage = codeMessage;
    this.args = args;
  }

  public AppException(AppErrorCodeMessageEnum codeMessage) {
    super();
    this.codeMessage = codeMessage;
    this.args = null;
  }

  public AppException(AppErrorCodeMessageEnum codeMessage, Serializable... args) {
    super();
    this.codeMessage = codeMessage;
    this.args = args;
  }
}
