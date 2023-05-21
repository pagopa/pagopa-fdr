package it.gov.pagopa.fdr.rest.exceptionmapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageInterface;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @Inject Logger log;

  @ServerExceptionMapper
  public Response mapWebApplicationException(WebApplicationException webApplicationException) {
    if (webApplicationException.getCause() instanceof JsonMappingException jsonMappingException) {
      return mapJsonMappingException(jsonMappingException).toResponse();
    } else if (webApplicationException.getCause()
        instanceof JsonParseException jsonParseException) {
      return mapJsonParseException(jsonParseException).toResponse();
    }
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

  private RestResponse<ErrorResponse> mapJsonMappingException(
      JsonMappingException jsonMappingException) {
    // quando jackson riesce a parsare il messaggio perchè non formato json valido

    AppException appEx =
        new AppException(
            jsonMappingException, AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_NON_VALID_FORMAT);

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

  private RestResponse<ErrorResponse> mapJsonParseException(JsonParseException jsonParseException) {
    // quando jackson riesce a parsare il messaggio perchè non formato json valido

    AppException appEx =
        new AppException(
            jsonParseException, AppErrorCodeMessageEnum.BAD_REQUEST_INPUT_JSON_NON_VALID_FORMAT);

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

  @SuppressWarnings("unchecked")
  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapInvalidFormatException(
      InvalidFormatException invalidFormatException) {
    // quando jackson riesce a parsare il messaggio per popolare il bean ma i valori NON sono
    // corretti
    String field =
        invalidFormatException.getPath().stream()
            .map(Reference::getFieldName)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("."));
    String currentValue = invalidFormatException.getValue().toString();
    AppException appEx = null;
    try {
      Class<?> target = Class.forName(invalidFormatException.getTargetType().getName());
      if (target.isEnum()) {
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) target;
        List<String> accepted = Stream.of(enumClass.getEnumConstants()).map(Enum::name).toList();
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
    // quando jackson NON riesce a parsare il messaggio per popolare il bean
    String field =
        mismatchedInputException.getPath().stream()
            .map(Reference::getFieldName)
            .filter(Objects::nonNull)
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
                        constraintViolation ->
                            ErrorResponse.ErrorMessage.builder()
                                .path(constraintViolation.getPropertyPath().toString())
                                .message(
                                    AppMessageUtil.getMessage(constraintViolation.getMessage()))
                                .build())
                    .collect(Collectors.toList()))
            .build());
  }
}
