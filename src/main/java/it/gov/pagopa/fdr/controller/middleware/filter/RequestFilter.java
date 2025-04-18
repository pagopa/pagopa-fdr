package it.gov.pagopa.fdr.controller.middleware.filter;

import static it.gov.pagopa.fdr.util.constant.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.EVENT_CATEGORY;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.FDR;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.HTTP_TYPE;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.ORGANIZATION_ID;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.PSP_ID;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.TRX_ID;
import static it.gov.pagopa.fdr.util.constant.MDCKeys.URI;

import it.gov.pagopa.fdr.service.ReService;
import it.gov.pagopa.fdr.service.model.re.EventTypeEnum;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import it.gov.pagopa.fdr.util.re.AppReUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.jboss.resteasy.reactive.server.jaxrs.ContainerRequestContextImpl;

@Provider
public class RequestFilter implements ContainerRequestFilter {

  private final Logger log;

  private final ReService reService;

  public RequestFilter(Logger log, ReService reService) {
    this.log = log;
    this.reService = reService;
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    long requestStartTime = System.nanoTime();
    containerRequestContext.setProperty("requestStartTime", requestStartTime);

    String sessionId = UUID.randomUUID().toString();

    String requestMethod = containerRequestContext.getMethod();
    String requestPath = containerRequestContext.getUriInfo().getAbsolutePath().getPath();
    String pspPathParam =
        containerRequestContext
            .getUriInfo()
            .getPathParameters()
            .getFirst(ControllerConstants.PARAMETER_PSP);
    String fdrPathParam =
        containerRequestContext
            .getUriInfo()
            .getPathParameters()
            .getFirst(ControllerConstants.PARAMETER_FDR);
    String ecPathParam =
        containerRequestContext
            .getUriInfo()
            .getPathParameters()
            .getFirst(ControllerConstants.PARAMETER_ORGANIZATION);

    FdrActionEnum fdrActionEnum =
        AppReUtil.getFlowNamebyAnnotation(
            ((ContainerRequestContextImpl) containerRequestContext)
                .getServerRequestContext()
                .getResteasyReactiveResourceInfo()
                .getAnnotations());

    String fdrAction = null;
    if (fdrActionEnum != null) {
      fdrAction = fdrActionEnum.name();
    }

    MultivaluedMap<String, String> pathparam =
        containerRequestContext.getUriInfo().getPathParameters();

    String subject = "NA";
    String pspId = null;
    String organizationId = null;
    if (!pathparam.isEmpty()) {
      if (pathparam.containsKey(ControllerConstants.PARAMETER_PSP)) {
        subject = pathparam.getFirst(ControllerConstants.PARAMETER_PSP);
        pspId = subject;
      } else if (pathparam.containsKey(ControllerConstants.PARAMETER_ORGANIZATION)) {
        subject = pathparam.getFirst(ControllerConstants.PARAMETER_ORGANIZATION);
        organizationId = subject;
      }
    }
    containerRequestContext.setProperty("subject", subject);

    putMDCReq(sessionId, fdrAction, requestPath, pspId, organizationId, fdrPathParam);

    String body =
        new BufferedReader(new InputStreamReader(containerRequestContext.getEntityStream()))
            .lines()
            .collect(Collectors.joining("\n"));
    containerRequestContext.setEntityStream(new ByteArrayInputStream(body.getBytes()));

    /*reService.sendEvent(
    ReInterface.builder()
        .serviceIdentifier(AppVersionEnum.FDR003)
        .created(Instant.now())
        .sessionId(sessionId)
        .eventType(EventTypeEnum.INTERFACE)
        .httpType(HttpTypeEnum.REQ)
        .httpMethod(requestMethod)
        .httpUrl(requestPath)
        .payload(body)
        .header(
            containerRequestContext.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
        .pspId(pspPathParam)
        .fdr(fdrPathParam)
        .organizationId(ecPathParam)
        .fdrAction(fdrActionEnum)
        .build());*/

    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERFACE.name());
    log.infof("REQ --> %s [uri:%s] [subject:%s]", requestMethod, requestPath, subject);
    MDC.remove(EVENT_CATEGORY);
  }

  private void putMDCReq(
      String sessionId,
      String action,
      String requestPath,
      String psp,
      String organizationId,
      String flowId) {
    MDC.put(TRX_ID, sessionId);
    MDC.put(HTTP_TYPE, AppConstant.REQUEST);
    MDC.put(ACTION, action != null ? action : "NA");
    MDC.put(URI, requestPath);
    MDC.put(PSP_ID, psp != null ? psp : "NA");
    MDC.put(ORGANIZATION_ID, organizationId != null ? organizationId : "NA");
    MDC.put(FDR, flowId != null ? flowId : "NA");
  }
}
