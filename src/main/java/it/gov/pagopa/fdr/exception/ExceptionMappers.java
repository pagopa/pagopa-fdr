package it.gov.pagopa.fdr.exception;

import it.gov.pagopa.fdr.rest.model.ErrorResponse;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @Inject Logger log;

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapException(AppException appEx) {
    AppErrorCodeMessageInterface codeMessage = appEx.getCodeMessage();
    RestResponse.Status status = codeMessage.httpStatus();

    return RestResponse.status(
        codeMessage.httpStatus(),
        ErrorResponse.builder()
            .httpStatusCode(status.getStatusCode())
            .httpStatusDescription(status.getReasonPhrase())
            .appErrorCode(codeMessage.errorCode())
            .errors(
                List.of(
                    ErrorResponse.ErrorMessage.builder()
                        .message(codeMessage.message(appEx.getArgs()))
                        .build()))
            .build());
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapException(Throwable throwable) {
    String errorId = UUID.randomUUID().toString();
    log.errorf(throwable, "Exception not managed - errorId[%s]", errorId);

    AppException appEx = new AppException(throwable, AppErrorCodeMessageEnum.ERROR);
    AppErrorCodeMessageInterface codeMessage = appEx.getCodeMessage();
    RestResponse.Status status = codeMessage.httpStatus();

    return RestResponse.status(
        codeMessage.httpStatus(),
        ErrorResponse.builder()
            .errorId(errorId)
            .httpStatusCode(status.getStatusCode())
            .httpStatusDescription(status.getReasonPhrase())
            .appErrorCode(codeMessage.errorCode())
            .errors(
                List.of(
                    ErrorResponse.ErrorMessage.builder()
                        .message(codeMessage.message(appEx.getArgs()))
                        .build()))
            .build());
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapException(
      ConstraintViolationException constraintViolationException) {

    AppException appEx =
        new AppException(constraintViolationException, AppErrorCodeMessageEnum.BAD_REQUEST);
    AppErrorCodeMessageInterface codeMessage = appEx.getCodeMessage();
    RestResponse.Status status = codeMessage.httpStatus();

    return RestResponse.status(
        codeMessage.httpStatus(),
        ErrorResponse.builder()
            .httpStatusCode(status.getStatusCode())
            .httpStatusDescription(status.getReasonPhrase())
            .appErrorCode(codeMessage.errorCode())
            .errors(
                constraintViolationException.getConstraintViolations().stream()
                    .map(
                        constraintViolation ->
                            ErrorResponse.ErrorMessage.builder()
                                .path(constraintViolation.getPropertyPath().toString())
                                .message(convertMessageKey(constraintViolation))
                                .build())
                    .collect(Collectors.toList()))
            .build());
  }

  private String convertMessageKey(ConstraintViolation constraintViolation) {
    String originalMessageKey = constraintViolation.getMessage();

    if (!originalMessageKey.contains("|")) {
      return AppMessageUtil.getMessage(originalMessageKey);
    } else {
      String[] messageToEvaluateSplit = originalMessageKey.split("\\|", 2);
      String messageKey = messageToEvaluateSplit[0];
      String[] args = messageToEvaluateSplit[1].split("\\|");
      return AppMessageUtil.getMessage(messageKey, args);
    }
  }
}
