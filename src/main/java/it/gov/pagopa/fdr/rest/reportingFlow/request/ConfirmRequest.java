package it.gov.pagopa.fdr.rest.reportingFlow.request;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Pagamento;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ConfirmRequest {
  private String reportingFlow;
  private Instant dateReportingFlow;
  private List<Pagamento> payments;
}
