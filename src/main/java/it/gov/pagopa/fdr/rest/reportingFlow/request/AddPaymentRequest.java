package it.gov.pagopa.fdr.rest.reportingFlow.request;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Pagamento;
import it.gov.pagopa.fdr.util.validation.ListSize;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class AddPaymentRequest {
  @NotNull(message = "reporting-flow.create.payments.notNull")
  @ListSize(min = 1, max = 100, message = "reporting-flow.create.payments.listSize|{min}|{max}")
  @Valid
  private List<Pagamento> payments;
}
