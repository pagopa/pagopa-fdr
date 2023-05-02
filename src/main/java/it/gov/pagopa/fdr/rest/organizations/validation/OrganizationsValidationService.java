package it.gov.pagopa.fdr.rest.organizations.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OrganizationsValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateGet(String id) {
    log.debug("Validate get");
  }

  @WithSpan(kind = SERVER)
  public void validateGetAllByEc(String idEc, String idPsp) {
    log.debug("Validate get all by ec");
  }
}
