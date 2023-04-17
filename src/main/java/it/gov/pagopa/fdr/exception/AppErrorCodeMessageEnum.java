package it.gov.pagopa.fdr.exception;

import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

public enum AppErrorCodeMessageEnum implements AppErrorCodeMessageInterface {
  ERROR("0500", "system.error", RestResponse.Status.INTERNAL_SERVER_ERROR),
  BAD_REQUEST("0400", "bad.request", RestResponse.Status.BAD_REQUEST),

  REPORTING_FLOW_NOT_FOUND("0701", "reporting-flow.notFound", RestResponse.Status.NOT_FOUND),
  REPORTING_FLOW_ID_INVALID("0702", "reporting-flow.idInvalid", Status.BAD_REQUEST);
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
