package it.gov.pagopa.fdr.rest.psps.request;

import it.gov.pagopa.fdr.rest.model.Payment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class AddPaymentRequest {
  @NotNull
  @Size(min = 1, max = 1000)
  @Valid
  private List<Payment> payments;
}
