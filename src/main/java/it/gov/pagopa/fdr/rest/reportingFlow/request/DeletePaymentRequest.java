package it.gov.pagopa.fdr.rest.reportingFlow.request;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DeletePaymentRequest {
  @NotNull
  @Size(min = 1, max = 100)
  private List<Long> indexPayments;
}
