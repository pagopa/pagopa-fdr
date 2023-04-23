package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Payment;
import java.math.BigDecimal;
import java.util.List;

public class ReportingFlowOnlyPayment {

  public List<Payment> payments;
  public Long count;

  public BigDecimal sum;
}
