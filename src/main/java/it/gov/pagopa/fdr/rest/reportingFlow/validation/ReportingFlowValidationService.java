package it.gov.pagopa.fdr.rest.reportingFlow.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.rest.reportingFlow.model.Payment;
import it.gov.pagopa.fdr.rest.reportingFlow.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.DeletePaymentRequest;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import it.gov.pagopa.fdr.service.reportingFlow.dto.PaymentDto;import org.jboss.logging.Logger;import java.util.Collections;import java.util.List;

@ApplicationScoped
public class ReportingFlowValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateCreateFlow(String psps, CreateFlowRequest createFlowRequest) {
    log.debug("Validate create");
    if(!psps.equals(createFlowRequest.getSender().getPspId())){
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_ID_NOT_MATCH,
          createFlowRequest.getReportingFlowName(),
          createFlowRequest.getSender().getPspId(),
          psps);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateAddPayment(String psps, String fdr, AddPaymentRequest addPaymentRequest) {
    log.debug("Validate add payment");

    long payment = addPaymentRequest.getPayments().stream().filter(a -> a.getPay() <= 0).count();
    if (payment > 0) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PAYMENT_PAYMENT_WRONG_IMPORT,
          fdr);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateDeletePayment(String psps, String fdr, DeletePaymentRequest deletePaymentRequest) {
    log.debug("Validate delete payment");

  }

  @WithSpan(kind = SERVER)
  public void validatePublish(String psps, String fdr) {
    log.debug("Validate publish");
  }

  @WithSpan(kind = SERVER)
  public void validateDelete(String psps, String fdr, ) {
    log.debug("Validate delete");
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
