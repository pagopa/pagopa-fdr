package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import it.gov.pagopa.fdr.repository.reportingFlow.Pagamento;
import java.math.BigDecimal;
import java.util.List;

public class ReportingFlowOnlyPayment {

  public List<Pagamento> payments;
  public Long count;

  public BigDecimal sum;
}
