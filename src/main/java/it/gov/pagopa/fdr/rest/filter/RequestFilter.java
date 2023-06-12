package it.gov.pagopa.fdr.rest.filter;

import static it.gov.pagopa.fdr.util.MDCKeys.TRX_ID;

import it.gov.pagopa.fdr.util.AppConstant;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.slf4j.MDC;

@Provider
public class RequestFilter implements ContainerRequestFilter {

  @Inject Logger log;

  @Override
  public void filter(ContainerRequestContext containerRequestContext) throws IOException {
    long requestStartTime = System.nanoTime();
    containerRequestContext.setProperty("requestStartTime", requestStartTime);
    MDC.put(TRX_ID, UUID.randomUUID().toString());

    MultivaluedMap<String, String> pathparam =
        containerRequestContext.getUriInfo().getPathParameters();

    String subject = "NA";
    if (!pathparam.isEmpty()) {
      if (pathparam.containsKey(AppConstant.PATH_PARAM_PSP)) {
        subject = pathparam.getFirst(AppConstant.PATH_PARAM_PSP);
      } else if (pathparam.containsKey(AppConstant.PATH_PARAM_EC)) {
        subject = pathparam.getFirst(AppConstant.PATH_PARAM_EC);
      }

      log.infof(
          "REQ --> %s [uri:%s] [subject:%s]",
          containerRequestContext.getMethod(),
          containerRequestContext.getUriInfo().getAbsolutePath().getPath(),
          subject);
    } else {
      log.infof(
          "REQ --> %s [uri:%s] [subject:%s]",
          containerRequestContext.getMethod(),
          containerRequestContext.getUriInfo().getAbsolutePath().getPath(),
          subject);
    }
    containerRequestContext.setProperty("subject", subject);
  }
}
