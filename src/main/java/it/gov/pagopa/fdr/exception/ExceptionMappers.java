package it.gov.pagopa.fdr.exception;

import it.gov.pagopa.fdr.rest.model.ErrorResponse;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class ExceptionMappers {

    @Inject
    Logger log;

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapException(AppException appEx) {
        AppErrorCodeMessageInterface codeMessage = appEx.getCodeMessage();
        RestResponse.Status status = codeMessage.httpStatus();

        ErrorResponse.ErrorMessage errorMessage = new ErrorResponse.ErrorMessage(codeMessage.message(appEx.getArgs()));
        ErrorResponse errorResponse = new ErrorResponse(status.getStatusCode(), status.getReasonPhrase(), codeMessage.errorCode(), errorMessage);

        return RestResponse.status(codeMessage.httpStatus(), errorResponse);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapException(Throwable throwable) {
        String errorId = UUID.randomUUID().toString();
        log.errorf(throwable, "Exception not managed - errorId[%s]", errorId);

        AppException appEx = new AppException(throwable, AppErrorCodeMessageEnum.ERROR);
        AppErrorCodeMessageInterface codeMessage = appEx.getCodeMessage();
        RestResponse.Status status = codeMessage.httpStatus();

        ErrorResponse.ErrorMessage errorMessage = new ErrorResponse.ErrorMessage(codeMessage.message(appEx.getArgs()));
        ErrorResponse errorResponse = new ErrorResponse(errorId, status.getStatusCode(), status.getReasonPhrase(), codeMessage.errorCode(), errorMessage);

        return RestResponse.status(codeMessage.httpStatus(), errorResponse);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapException(ConstraintViolationException constraintViolationException) {
        AppException appEx = new AppException(constraintViolationException, AppErrorCodeMessageEnum.BAD_REQUEST);
        AppErrorCodeMessageInterface codeMessage = appEx.getCodeMessage();
        RestResponse.Status status = codeMessage.httpStatus();

        List<ErrorResponse.ErrorMessage> errorMessages = constraintViolationException.getConstraintViolations().stream()
                .map(constraintViolation -> new ErrorResponse.ErrorMessage(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()))
                .collect(Collectors.toList());
        ErrorResponse errorResponse = new ErrorResponse(status.getStatusCode(), status.getReasonPhrase(), codeMessage.errorCode(), errorMessages);

        return RestResponse.status(codeMessage.httpStatus(), errorResponse);
    }



}
