package it.gov.pagopa.fdr.rest.filter;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.EC_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.FLOW_NAME;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.TRX_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse;
import it.gov.pagopa.fdr.rest.exceptionmapper.ErrorResponse.ErrorMessage;
import it.gov.pagopa.fdr.service.re.ReService;
import it.gov.pagopa.fdr.service.re.model.AppVersionEnum;
import it.gov.pagopa.fdr.service.re.model.EventTypeEnum;
import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import it.gov.pagopa.fdr.service.re.model.HttpTypeEnum;
import it.gov.pagopa.fdr.service.re.model.ReInterface;
import it.gov.pagopa.fdr.util.AppReUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.jaxrs.ContainerRequestContextImpl;
import org.slf4j.MDC;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

  @Inject Logger log;

  @Inject ReService reService;

  @Inject ObjectMapper objectMapper;

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (requestContext.getPropertyNames().contains("requestStartTime")) {
      long requestStartTime = (long) requestContext.getProperty("requestStartTime");
      long requestFinishTime = System.nanoTime();
      long elapsed = TimeUnit.NANOSECONDS.toMillis(requestFinishTime - requestStartTime);
      String requestSubject = (String) requestContext.getProperty("subject");
      String action = MDC.get(ACTION);
      String psp = MDC.get(PSP_ID);
      String ec = MDC.get(EC_ID);
      String flow = MDC.get(FLOW_NAME);

      FlowActionEnum flowActionEnum =
          AppReUtil.getFlowNamebyAnnotation(
              ((ContainerRequestContextImpl) requestContext)
                  .getServerRequestContext()
                  .getResteasyReactiveResourceInfo()
                  .getAnnotations());

      if (flowActionEnum == null) {
        log.warn("Attention, missing annotation Re on this action");
      }

      String sessionId = MDC.get(TRX_ID);
      String requestMethod = requestContext.getMethod();
      String requestPath = requestContext.getUriInfo().getAbsolutePath().getPath();

      String responsePayload = null;
      try {
        responsePayload = objectMapper.writeValueAsString(responseContext.getEntity());
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      reService.sendEvent(
          ReInterface.builder()
              .appVersion(AppVersionEnum.FDR003)
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
              .flowName(flow)
              .organizationId(ec)
              .flowAction(flowActionEnum)
              .build());

      int httpStatus = responseContext.getStatus();
      Optional<ErrorResponse> errorResponse = Optional.empty();

      if (responseContext.getStatus() != Response.Status.OK.getStatusCode()
          && responseContext.getStatus() != Status.CREATED.getStatusCode()) {
        Object body = responseContext.getEntity();
        if (body instanceof ErrorResponse) {
          errorResponse = Optional.of((ErrorResponse) body);
          logErrorResponse(
              requestMethod, requestPath, requestSubject, elapsed, errorResponse.get());
        } else {
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

      logJsonReq(action, requestPath, psp, ec);
      logJsonRes(action, requestPath, psp, ec, elapsed, httpStatus, errorResponse);

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

  private void logJsonReq(String action, String requestPath, String psp, String ec) {
    log.infof(
        jsonStringOperation(
            action, requestPath, psp, ec, Optional.empty(), Optional.empty(), Optional.empty()));
  }

  private void logJsonRes(
      String action,
      String requestPath,
      String psp,
      String ec,
      Long elapsed,
      Integer httpStatus,
      Optional<ErrorResponse> errorResponse) {
    log.infof(
        jsonStringOperation(
            action,
            requestPath,
            psp,
            ec,
            Optional.of(elapsed),
            Optional.of(httpStatus),
            errorResponse));
  }

  private String jsonStringOperation(
      String action,
      String requestPath,
      String psp,
      String ec,
      Optional<Long> elapsed,
      Optional<Integer> statusCode,
      Optional<ErrorResponse> errorResponse) {
    StringBuilder stringBuilder = new StringBuilder("{");
    stringBuilder.append("\"isJsonLog\":\"%s\"".formatted(true));
    statusCode.ifPresentOrElse(
        sc -> {
          stringBuilder.append(",\"httpType\":\"RES\"");
          errorResponse.ifPresentOrElse(
              er -> {
                stringBuilder.append(",\"outcome\":\"KO\"");
                stringBuilder.append(",\"code\":\"%s\"".formatted(er.getAppErrorCode()));
                stringBuilder.append(
                    ",\"message\":\"%s\""
                        .formatted(
                            er.getErrors().stream()
                                .map(ErrorMessage::getMessage)
                                .collect(Collectors.joining(", "))));
              },
              () -> stringBuilder.append(",\"outcome\":\"OK\""));
        },
        () -> stringBuilder.append(",\"httpType\":\"REQ\""));
    stringBuilder.append(",\"action\":%s".formatted(action != null ? action : "NA"));
    stringBuilder.append(",\"uri\":\"%s\"".formatted(requestPath));
    elapsed.ifPresent(v -> stringBuilder.append(",\"elapsed\":%d".formatted(v)));
    statusCode.ifPresent(s -> stringBuilder.append(",\"statusCode\":%d".formatted(s)));
    stringBuilder.append(",\"psp\":\"%s\"".formatted(psp != null ? psp : "NA"));
    stringBuilder.append(",\"ec\":\"%s\"".formatted(ec != null ? ec : "NA"));
    stringBuilder.append("}");

    return stringBuilder.toString();
  }
}
