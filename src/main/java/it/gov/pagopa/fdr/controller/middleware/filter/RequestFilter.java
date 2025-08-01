package it.gov.pagopa.fdr.controller.middleware.filter;

import static it.gov.pagopa.fdr.util.constant.MDCKeys.*;

import it.gov.pagopa.fdr.service.model.re.EventTypeEnum;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import it.gov.pagopa.fdr.util.common.StringUtil;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.jboss.resteasy.reactive.server.jaxrs.ContainerRequestContextImpl;

@Provider
public class RequestFilter implements ContainerRequestFilter {

  @ConfigProperty(name = "registro-eventi.exclude-from-save.actions")
  private Set<String> actionsExcludedFromSave;

  private final Logger log;

  public RequestFilter(Logger log) {
    this.log = log;
  }

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {

    // Generating values for session
    long requestStartTime = System.nanoTime();
    String sessionId = UUID.randomUUID().toString();

    // Extracting info from request URL
    String requestMethod = containerRequestContext.getMethod();
    String requestPath = containerRequestContext.getUriInfo().getAbsolutePath().getPath();

    // Extracting path parameters
    MultivaluedMap<String, String> pathParameters =
        containerRequestContext.getUriInfo().getPathParameters();
    String pspPathParam = pathParameters.getFirst(ControllerConstants.PARAMETER_PSP);
    String flowPathParam = pathParameters.getFirst(ControllerConstants.PARAMETER_FDR);
    String organizationPathParam =
        pathParameters.getFirst(ControllerConstants.PARAMETER_ORGANIZATION);

    // Defining subject and start time, needed for ResponseFilter
    String subject = "NA";
    if (!pathParameters.isEmpty()) {
      if (pathParameters.containsKey(ControllerConstants.PARAMETER_PSP)) {
        subject = pathParameters.getFirst(ControllerConstants.PARAMETER_PSP);
      } else if (pathParameters.containsKey(ControllerConstants.PARAMETER_ORGANIZATION)) {
        subject = pathParameters.getFirst(ControllerConstants.PARAMETER_ORGANIZATION);
      }
    }
    containerRequestContext.setProperty("subject", subject);
    containerRequestContext.setProperty("requestStartTime", requestStartTime);

    // Extract FdrAction value and store on Registro Eventi IF AND ONLY IF this value
    // (extracted from existing @Re annotation in controller) is set!
    FdrActionEnum fdrActionEnum =
        AppReUtil.getFdrActionByAnnotation(
            ((ContainerRequestContextImpl) containerRequestContext)
                .getServerRequestContext()
                .getResteasyReactiveResourceInfo()
                .getAnnotations());
    boolean isActionIncludedForRE = isActionIncludedForRE(fdrActionEnum);
    if (isActionIncludedForRE) {

      // Extracting request body in order to be lately stored in BLOB Storage
      String fdrAction = fdrActionEnum.name();
      String body =
          new BufferedReader(new InputStreamReader(containerRequestContext.getEntityStream()))
              .lines()
              .collect(Collectors.joining("\n"));
      containerRequestContext.setEntityStream(new ByteArrayInputStream(body.getBytes()));

      // Set request values as properties
      containerRequestContext.setProperty("parsedRequest", body);
      containerRequestContext.setProperty("fdrAction", fdrAction);
    }

    // Logging request execution
    putRequestInfoInMDC(
        sessionId, fdrActionEnum, requestPath, pspPathParam, organizationPathParam, flowPathParam);
    MDC.put(IS_RE_ENABLED_FOR_THIS_CALL, isActionIncludedForRE ? "1" : "0");
    log.infof(
        "REQ --> %s [uri:%s] [subject:%s]",
        requestMethod, StringUtil.sanitize(requestPath), StringUtil.sanitize(subject));
    MDC.remove(EVENT_CATEGORY);
  }

  private void putRequestInfoInMDC(
      String sessionId,
      FdrActionEnum action,
      String requestPath,
      String psp,
      String organizationId,
      String flowId) {

    MDC.put(EVENT_CATEGORY, EventTypeEnum.INTERFACE.name());
    MDC.put(HTTP_TYPE, AppConstant.REQUEST);
    MDC.put(TRX_ID, sessionId);
    MDC.put(ACTION, action != null ? action.name() : "NA");
    MDC.put(URI, requestPath);
    MDC.put(PSP_ID, psp != null ? psp : "NA");
    MDC.put(ORGANIZATION_ID, organizationId != null ? organizationId : "NA");
    MDC.put(FDR, flowId != null ? flowId : "NA");
  }

  private boolean isActionIncludedForRE(FdrActionEnum fdrActionEnum) {
    return fdrActionEnum != null && !actionsExcludedFromSave.contains(fdrActionEnum.name());
  }
}
