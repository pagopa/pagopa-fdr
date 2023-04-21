package it.gov.pagopa.fdr.rest.reportingFlow.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.reportingFlow.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateRequest;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportingFlowValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateCreate(CreateRequest createRequest) {
    log.debug("Validate create");
  }

  @WithSpan(kind = SERVER)
  public void validateAddPayment(AddPaymentRequest addPaymentRequest) {
    log.debug("Validate add payment");
  }

  @WithSpan(kind = SERVER)
  public void validateConfirm(String id) {
    log.debug("Validate confirm");
  }

  @WithSpan(kind = SERVER)
  public void validateGet(String id) {
    log.debug("Validate get");
  }

  @WithSpan(kind = SERVER)
  public void validateGetAllByEc(String idEc, String idPsp) {
    log.debug("Validate get all by ec");
  }
}
