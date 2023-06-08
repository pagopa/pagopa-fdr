package it.gov.pagopa.fdr.rest.filter;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.EC_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse.ErrorMessage;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.mockserver.model.HttpStatusCode;
import org.slf4j.MDC;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

  @Inject Logger log;

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    long requestStartTime = (long) requestContext.getProperty("requestStartTime");
    long requestFinishTime = System.nanoTime();
    long elapsed = TimeUnit.NANOSECONDS.toMillis(requestFinishTime - requestStartTime);
    String requestSubject = (String) requestContext.getProperty("subject");
    String action = MDC.get(ACTION);
    String psp = MDC.get(PSP_ID);
    String ec = MDC.get(EC_ID);

    String requestMethod = requestContext.getMethod();
    String requestPath = requestContext.getUriInfo().getAbsolutePath().getPath();
    int httpStatus = responseContext.getStatus();
    boolean isSuccess = false;
    Optional<ErrorResponse> errorResponse = Optional.empty();

    if (responseContext.getStatus() != HttpStatusCode.OK_200.code()
        && responseContext.getStatus() != HttpStatusCode.CREATED_201.code()) {
      Object body = responseContext.getEntity();
      if (body instanceof ErrorResponse) {
        errorResponse = Optional.of((ErrorResponse) body);
        logErrorResponse(requestMethod, requestPath, requestSubject, elapsed, errorResponse.get());
      } else {
        isSuccess = true;
        log.infof(
            "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d] [description:%s]",
            requestMethod,
            requestPath,
            requestSubject,
            elapsed,
            httpStatus,
            ((Throwable) responseContext.getEntity()).getMessage());
      }
    } else {
      log.infof(
          "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d]",
          requestMethod, requestPath, requestSubject, elapsed, httpStatus);
    }

    log.infof(
        jsonLog(
            "REQ",
            action,
            psp,
            ec,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()));
    log.infof(
        jsonLog(
            "RES",
            action,
            psp,
            ec,
            Optional.of(elapsed),
            Optional.of(httpStatus),
            Optional.of(isSuccess),
            errorResponse));

    MDC.clear();
  }

  private void logErrorResponse(
      String requestMethod,
      String requestPath,
      String subject,
      long elapsed,
      ErrorResponse errorResponse) {
    log.infof(
        "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d] [appErrorCode:%s]"
            + " [description:%s]",
        requestMethod,
        requestPath,
        subject,
        elapsed,
        errorResponse.getHttpStatusCode(),
        errorResponse.getAppErrorCode(),
        errorResponse.getErrors().stream()
            .map(ErrorMessage::getMessage)
            .collect(Collectors.joining(", ")));
  }

  private String jsonLog(
      String httpType,
      String action,
      String psp,
      String ec,
      Optional<Long> elapsed,
      Optional<Integer> statusCode,
      Optional<Boolean> isSuccess,
      Optional<ErrorResponse> errorResponse) {
    StringBuilder stringBuilder = new StringBuilder("{");
    stringBuilder.append("\"httpType\":\"%s\"".formatted(httpType));
    stringBuilder.append(",\"action\":%s".formatted(action));
    elapsed.map(v -> stringBuilder.append(",\"elapsed\":%d".formatted(v)));
    statusCode.map(s -> stringBuilder.append(",\"statusCode\":%d".formatted(s)));
    isSuccess.map(
        (success) -> {
          if (success) {
            stringBuilder.append(",\"outcome\":\"OK\"");
          } else {
            stringBuilder.append(",\"outcome\":\"KO\"");
            errorResponse.map(
                er -> {
                  stringBuilder.append(",\"code\":\"%s\"".formatted(er.getAppErrorCode()));
                  stringBuilder.append(
                      ",\"message\":\"%s\""
                          .formatted(
                              er.getErrors().stream()
                                  .map(ErrorMessage::getMessage)
                                  .collect(Collectors.joining(", "))));
                  return null;
                });
          }
          return null;
        });
    stringBuilder.append(",\"psp\":\"%s\"".formatted(psp));
    stringBuilder.append(",\"ec\":\"%s\"".formatted(ec));
    stringBuilder.append("}");

    return stringBuilder.toString();
  }
}
