package it.gov.pagopa.fdr.controller.middleware.filter;

import static it.gov.pagopa.fdr.util.constant.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.ELAPSED;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.EVENT_CATEGORY;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.FDR;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.HTTP_TYPE;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.ORGANIZATION_ID;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.PSP_ID;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.STATUS_CODE;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.TRX_ID;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.controller.model.error.ErrorMessage;
import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.service.ReService;
import it.gov.pagopa.fdr.service.model.re.EventTypeEnum;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import it.gov.pagopa.fdr.util.constant.MDCKeys;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import it.gov.pagopa.fdr.util.re.AppReUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.jboss.resteasy.reactive.server.jaxrs.ContainerRequestContextImpl;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

  private final Logger log;

  private final ReService reService;

  private final ObjectMapper objectMapper;

  public ResponseFilter(Logger log, ReService reService, ObjectMapper objectMapper) {
    this.log = log;
    this.reService = reService;
    this.objectMapper = objectMapper;
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (requestContext.getPropertyNames().contains("requestStartTime")) {
      long requestStartTime = (long) requestContext.getProperty("requestStartTime");
      long requestFinishTime = System.nanoTime();
      long elapsed = TimeUnit.NANOSECONDS.toMillis(requestFinishTime - requestStartTime);
      String requestSubject = (String) requestContext.getProperty("subject");
      String action = (String) MDC.get(ACTION);
      String psp = (String) MDC.get(PSP_ID);
      String organizationId = (String) MDC.get(ORGANIZATION_ID);
      String fdr = (String) MDC.get(FDR);

      FdrActionEnum fdrActionEnum =
          AppReUtil.getFlowNamebyAnnotation(
              ((ContainerRequestContextImpl) requestContext)
                  .getServerRequestContext()
                  .getResteasyReactiveResourceInfo()
                  .getAnnotations());

      String sessionId = (String) MDC.get(TRX_ID);
      String requestMethod = requestContext.getMethod();
      String requestPath = requestContext.getUriInfo().getAbsolutePath().getPath();

      int httpStatus = responseContext.getStatus();
      Optional<ErrorResponse> errorResponse = Optional.empty();

      if (responseContext.getStatus() != Response.Status.OK.getStatusCode()
          && responseContext.getStatus() != Status.CREATED.getStatusCode()) {
        Object body = responseContext.getEntity();
        if (body instanceof ErrorResponse) {
          errorResponse = Optional.of((ErrorResponse) body);
        }
      }
      putMDCRes(action, requestPath, psp, organizationId, elapsed, httpStatus, errorResponse);

      String responsePayload;
      try {
        responsePayload = objectMapper.writeValueAsString(responseContext.getEntity());
      } catch (JsonProcessingException e) {
        throw new AppException(e, AppErrorCodeMessageEnum.ERROR);
      }

      /*reService.sendEvent(
      ReInterface.builder()
          .serviceIdentifier(AppVersionEnum.FDR003)
          .created(Instant.now())
          .sessionId(sessionId)
          .eventType(EventTypeEnum.INTERFACE)
          .httpType(HttpTypeEnum.RES)
          .httpMethod(requestMethod)
          .httpUrl(requestPath)
          .payload(responsePayload)
          .header(
              responseContext.getHeaders().entrySet().stream()
                  .collect(
                      Collectors.toMap(
                          Map.Entry::getKey,
                          a -> Stream.of(a.getValue()).map(Object::toString).toList())))
          .pspId(psp)
          .fdr(fdr)
          .organizationId(organizationId)
          .fdrAction(fdrActionEnum)
          .build());*/

      MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERFACE.name());
      if (responseContext.getStatus() != Response.Status.OK.getStatusCode()
          && responseContext.getStatus() != Status.CREATED.getStatusCode()) {
        Object body = responseContext.getEntity();
        if (body instanceof ErrorResponse) {
          errorResponse = Optional.of((ErrorResponse) body);
          logErrorResponse(
              requestMethod, requestPath, requestSubject, elapsed, errorResponse.get());
        } else {
          String message = null;
          if (responseContext.getEntity() != null) {
            message = ((Throwable) responseContext.getEntity()).getMessage();
          }
          log.infof(
              "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d] [description:%s]",
              requestMethod, requestPath, requestSubject, elapsed, httpStatus, message);
        }
      } else {
        log.infof(
            "RES --> %s [uri:%s] [subject:%s] [elapsed:%dms] [statusCode:%d]",
            requestMethod, requestPath, requestSubject, elapsed, httpStatus);
      }

      MDC.clear();
    }
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

  private void putMDCRes(
      String action,
      String requestPath,
      String psp,
      String organizationId,
      Long elapsed,
      Integer httpStatus,
      Optional<ErrorResponse> errorResponse) {
    MDC.put(HTTP_TYPE, AppConstant.RESPONSE);
    if (errorResponse.isPresent()) {
      MDC.put(MDCKeys.OUTCOME, AppConstant.KO);
      MDC.put(MDCKeys.CODE, errorResponse.get().getAppErrorCode());
      MDC.put(
          MDCKeys.MESSAGE,
          errorResponse.get().getErrors().stream()
              .map(ErrorMessage::getMessage)
              .collect(Collectors.joining(", ")));
    } else {
      MDC.put(MDCKeys.OUTCOME, AppConstant.OK);
    }
    MDC.put(ACTION, action != null ? action : "NA");
    MDC.put(URI, requestPath);
    MDC.put(ELAPSED, elapsed);
    MDC.put(STATUS_CODE, httpStatus);
    MDC.put(PSP_ID, psp != null ? psp : "NA");
    MDC.put(ORGANIZATION_ID, organizationId != null ? organizationId : "NA");
  }
}
