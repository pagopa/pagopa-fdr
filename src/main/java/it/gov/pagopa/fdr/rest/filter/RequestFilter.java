package it.gov.pagopa.fdr.rest.filter;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.TRX_ID;

import it.gov.pagopa.fdr.service.re.ReService;
import it.gov.pagopa.fdr.service.re.model.AppVersionEnum;
import it.gov.pagopa.fdr.service.re.model.EventTypeEnum;
import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import it.gov.pagopa.fdr.service.re.model.HttpTypeEnum;
import it.gov.pagopa.fdr.service.re.model.ReInterface;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppReUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.jaxrs.ContainerRequestContextImpl;
import org.slf4j.MDC;

@Provider
public class RequestFilter implements ContainerRequestFilter {

  @Inject Logger log;

  @Inject ReService reService;

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    long requestStartTime = System.nanoTime();
    containerRequestContext.setProperty("requestStartTime", requestStartTime);

    String sessionId = UUID.randomUUID().toString();
    MDC.put(TRX_ID, sessionId);

    String requestMethod = containerRequestContext.getMethod();
    String requestPath = containerRequestContext.getUriInfo().getAbsolutePath().getPath();
    String pspPathParam = containerRequestContext.getUriInfo().getPathParameters().getFirst("psp");
    String flowPathParam = containerRequestContext.getUriInfo().getPathParameters().getFirst("fdr");
    String ecPathParam = containerRequestContext.getUriInfo().getPathParameters().getFirst("ec");

    FlowActionEnum flowActionEnum =
        AppReUtil.getFlowNamebyAnnotation(
            ((ContainerRequestContextImpl) containerRequestContext)
                .getServerRequestContext()
                .getResteasyReactiveResourceInfo()
                .getAnnotations());

    if (flowActionEnum == null) {
      log.warn("Attention, missing annotation Re on this action");
    } else {
      MDC.put(ACTION, flowActionEnum.name());
    }

    String body =
        new BufferedReader(new InputStreamReader(containerRequestContext.getEntityStream()))
            .lines()
            .collect(Collectors.joining("\n"));
    containerRequestContext.setEntityStream(new ByteArrayInputStream(body.getBytes()));

    reService.sendEvent(
        ReInterface.builder()
            .appVersion(AppVersionEnum.FDR003)
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
            .flowName(flowPathParam)
            .organizationId(ecPathParam)
            .flowAction(flowActionEnum)
            .build());

    MultivaluedMap<String, String> pathparam =
        containerRequestContext.getUriInfo().getPathParameters();

    String subject = "NA";
    if (!pathparam.isEmpty()) {
      if (pathparam.containsKey(AppConstant.PATH_PARAM_PSP)) {
        subject = pathparam.getFirst(AppConstant.PATH_PARAM_PSP);
      } else if (pathparam.containsKey(AppConstant.PATH_PARAM_EC)) {
        subject = pathparam.getFirst(AppConstant.PATH_PARAM_EC);
      }

      log.infof("REQ --> %s [uri:%s] [subject:%s]", requestMethod, requestPath, subject);
    } else {
      log.infof("REQ --> %s [uri:%s] [subject:%s]", requestMethod, requestPath, subject);
    }
    containerRequestContext.setProperty("subject", subject);
  }
}
