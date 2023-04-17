package it.gov.pagopa.fdr.rest.exceptionMapper;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageInterface;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.UnexpectedTypeException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @Inject Logger log;

  @ServerExceptionMapper
  public Response mapWebApplicationException(WebApplicationException webApplicationException) {
    return webApplicationException.getResponse();
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapAppException(AppException appEx) {
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
  public RestResponse<ErrorResponse> mapInvalidFormatException(
      InvalidFormatException invalidFormatException) {

    String field =
        invalidFormatException.getPath().stream()
            .map(a -> a.getFieldName())
            .collect(Collectors.joining("."));
    String currentValue = invalidFormatException.getValue().toString();
    AppException appEx = null;
    try {
      Class<?> target = Class.forName(invalidFormatException.getTargetType().getName());
      if (target.isEnum()) {
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) target;
        List<String> accepted =
            Stream.of(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
        appEx =
            new AppException(
                invalidFormatException,
                AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_ENUM,
                field,
                currentValue,
                accepted);
      } else if (target.isAssignableFrom(Instant.class)) {
        appEx =
            new AppException(
                invalidFormatException,
                AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_INSTANT,
                field,
                currentValue);
      } else {
        appEx =
            new AppException(
                invalidFormatException,
                AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON,
                field,
                currentValue);
      }

    } catch (ClassNotFoundException e) {
      appEx =
          new AppException(
              invalidFormatException,
              AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON,
              field,
              currentValue);
    }

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
  public RestResponse<ErrorResponse> mapMismatchedInputException(
      MismatchedInputException mismatchedInputException) {

    String field =
        mismatchedInputException.getPath().stream()
            .map(a -> a.getFieldName())
            .collect(Collectors.joining("."));
    AppException appEx =
        new AppException(
            mismatchedInputException,
            AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_DESERIALIZE_ERROR,
            field);

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
  public RestResponse<ErrorResponse> mapUnexpectedTypeException(UnexpectedTypeException exception) {
    return mapThrowable(exception);
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapThrowable(Throwable exception) {
    String errorId = UUID.randomUUID().toString();
    log.errorf(exception, "Exception not managed - errorId[%s]", errorId);

    AppException appEx = new AppException(exception, AppErrorCodeMessageEnum.ERROR);
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
  public RestResponse<ErrorResponse> mapConstraintViolationException(
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
                    .sorted(
                        (a, b) ->
                            b.getPropertyPath()
                                .toString()
                                .compareTo(a.getPropertyPath().toString()))
                    .map(
                        constraintViolation -> {
                          log.info(constraintViolation.getPropertyPath().toString());
                          return ErrorResponse.ErrorMessage.builder()
                              .path(constraintViolation.getPropertyPath().toString())
                              .message(convertMessageKey(constraintViolation))
                              .build();
                        })
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
