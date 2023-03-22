package it.gov.pagopa.fdr.exception;

import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import org.jboss.resteasy.reactive.RestResponse;

public enum AppErrorCodeMessageEnum implements AppErrorCodeMessageInterface {
  ERROR("0500", "system.error", RestResponse.Status.INTERNAL_SERVER_ERROR),
  BAD_REQUEST("0400", "bad.request", RestResponse.Status.BAD_REQUEST),
  FRUIT_BAD_REQUEST("0600", "fruit.name.notMapped", RestResponse.Status.BAD_REQUEST),
  FRUIT_NOT_FOUND("0601", "fruit.name.notFound", RestResponse.Status.NOT_FOUND);

  private final String errorCode;
  private final String errorMessageKey;
  private final RestResponse.Status httpStatus;

  AppErrorCodeMessageEnum(
      String errorCode, String errorMessageKey, RestResponse.Status httpStatus) {
    this.errorCode = errorCode;
    this.errorMessageKey = errorMessageKey;
    this.httpStatus = httpStatus;
  }

  @Override
  public String errorCode() {
    return AppConstant.SERVICE_CODE_APP + "-" + errorCode;
  }

  @Override
  public String message(Object... args) {
    return AppMessageUtil.getMessage(errorMessageKey, args);
  }

  @Override
  public RestResponse.Status httpStatus() {
    return httpStatus;
  }
}
