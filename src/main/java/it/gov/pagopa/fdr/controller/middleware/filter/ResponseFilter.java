package it.gov.pagopa.fdr.controller.middleware.filter;

import static it.gov.pagopa.fdr.util.constant.MDCKeys.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.controller.model.error.ErrorMessage;
import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.service.ReService;
import it.gov.pagopa.fdr.service.model.re.AppVersionEnum;
import it.gov.pagopa.fdr.service.model.re.EventTypeEnum;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import it.gov.pagopa.fdr.service.model.re.ReEvent;
import it.gov.pagopa.fdr.util.common.StringUtil;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import it.gov.pagopa.fdr.util.constant.MDCKeys;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

  private final Logger log;

  private final ReService reService;

  private final ObjectMapper objectMapper;

  private static final Set<Integer> OK_RESPONSE_CODES =
      Set.of(Status.OK.getStatusCode(), Status.CREATED.getStatusCode());

  public ResponseFilter(Logger log, ReService reService, ObjectMapper objectMapper) {
    this.log = log;
    this.reService = reService;
    this.objectMapper = objectMapper;
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

    boolean canBeWrittenAsREEvent =
        "1".equals(Optional.ofNullable(MDC.get(IS_RE_ENABLED_FOR_THIS_CALL)).orElse("0"));
    if (canBeWrittenAsREEvent && requestContext.getPropertyNames().contains("fdrAction")) {

      //
      long requestStartTime = (long) requestContext.getProperty("requestStartTime");
      long requestFinishTime = System.nanoTime();
      long elapsed = TimeUnit.NANOSECONDS.toMillis(requestFinishTime - requestStartTime);

      // Extracting info from request and response
      String requestMethod = requestContext.getMethod();
      String requestPath = requestContext.getUriInfo().getAbsolutePath().getPath();
      int httpStatus = responseContext.getStatus();

      // Extracting info from context properties
      String requestSubject = (String) requestContext.getProperty("subject");
      String fdrAction = (String) requestContext.getProperty("fdrAction");

      // Populate MDC for include values on logged elements as MDC.<field>
      Object responseContextEntity = responseContext.getEntity();
      putResponseInfoInMDC(fdrAction, requestPath, elapsed, httpStatus, responseContextEntity);

      // Extracting request and response payloads
      String requestPayload = (String) requestContext.getProperty("parsedRequest");

      String responsePayload;
      try {
        responsePayload = objectMapper.writeValueAsString(responseContextEntity);
      } catch (JsonProcessingException e) {
        throw new AppException(e, AppErrorCodeMessageEnum.ERROR);
      }

      // Generate events on Registro Eventi and also store on BLOB storage
      generateAndSendREEvent(
          requestContext, fdrAction, requestPayload, responsePayload, requestMethod, requestPath);

      // Finally, log all response info with also MDC values, then clear MDC context
      logResponse(responseContext, requestMethod, requestPath, requestSubject, elapsed, httpStatus);
      MDC.clear();
    }
  }

  private void generateAndSendREEvent(
      ContainerRequestContext requestContext,
      String fdrAction,
      String requestPayload,
      String responsePayload,
      String requestMethod,
      String requestPath) {

    // Then, send the event: the payload will be saved on BLOB Storage, the event in MongoDB
    reService.sendEvent(
        ReEvent.builder()
            .serviceIdentifier(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId((String) MDC.get(TRX_ID))
            .eventType(EventTypeEnum.INTERFACE)
            .httpMethod(requestMethod)
            .httpUrl(requestPath)
            .reqPayload(requestPayload)
            .resPayload(responsePayload)
            .header(
                requestContext.getHeaders().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            .pspId((String) MDC.get(PSP_ID))
            .fdr((String) MDC.get(FDR))
            .organizationId((String) MDC.get(ORGANIZATION_ID))
            .fdrAction(FdrActionEnum.valueOf(fdrAction))
            .build());
  }

  private void logResponse(
      ContainerResponseContext responseContext,
      String requestMethod,
      String requestPath,
      String requestSubject,
      long elapsed,
      int httpStatus) {

    if (OK_RESPONSE_CODES.contains(responseContext.getStatus())) {

      Object body = responseContext.getEntity();
      if (body instanceof ErrorResponse errorResponse) {

        log.infof(
            "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d] [appErrorCode:%s]"
                + " [description:%s]",
            requestMethod,
            StringUtil.sanitize(requestPath),
            StringUtil.sanitize(requestSubject),
            elapsed,
            errorResponse.getHttpStatusCode(),
            errorResponse.getAppErrorCode(),
            errorResponse.getErrors().stream()
                .map(ErrorMessage::getMessage)
                .collect(Collectors.joining(", ")));
      } else {

        String message = null;
        Object responseEntity = responseContext.getEntity();
        if (responseEntity instanceof Throwable throwable) {
          message = throwable.getMessage();
        }
        log.infof(
            "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d] [description:%s]",
            requestMethod,
            StringUtil.sanitize(requestPath),
            StringUtil.sanitize(requestSubject),
            elapsed,
            httpStatus,
            StringUtil.sanitize(message));
      }

    } else {
      log.infof(
          "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d]",
          requestMethod,
          StringUtil.sanitize(requestPath),
          StringUtil.sanitize(requestSubject),
          elapsed,
          httpStatus);
    }
  }

  private void putResponseInfoInMDC(
      String action, String requestPath, Long elapsed, Integer httpStatus, Object response) {

    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERFACE.name());
    MDC.put(HTTP_TYPE, AppConstant.RESPONSE);
    MDC.put(ACTION, action != null ? action : "NA");
    MDC.put(URI, requestPath);
    MDC.put(ELAPSED, elapsed);
    MDC.put(STATUS_CODE, httpStatus);

    if (MDC.get(PSP_ID) == null) {
      MDC.put(PSP_ID, "NA");
    }
    if (MDC.get(ORGANIZATION_ID) == null) {
      MDC.put(ORGANIZATION_ID, "NA");
    }

    if (response instanceof ErrorResponse errResponse) {

      Optional<ErrorResponse> errorResponse = Optional.of(errResponse);
      MDC.put(MDCKeys.OUTCOME, AppConstant.KO);
      MDC.put(MDCKeys.CODE, errorResponse.get().getAppErrorCode());
      // don't modify error message to be able to trace validation errors too
      MDC.put(
              MDCKeys.MESSAGE,
              errorResponse.get().getErrors().stream().map(e -> e.getMessage()).collect(Collectors.joining(", ")) );

    } else {
      MDC.put(MDCKeys.OUTCOME, AppConstant.OK);
    }
  }
}
