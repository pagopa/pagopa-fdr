package it.gov.pagopa.fdr.rest.reportingFlow.request;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Pagamento;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class AddPaymentRequest {
  @NotNull
  @Size(min = 1, max = 100)
  @Valid
  private List<Pagamento> payments;
}
