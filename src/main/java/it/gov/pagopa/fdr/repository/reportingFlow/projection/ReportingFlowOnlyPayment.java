package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.reportingFlow.Pagamento;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import java.util.List;

@ProjectionFor(ReportingFlow.class)
public class ReportingFlowOnlyPayment {

  public List<Pagamento> payments;
}
