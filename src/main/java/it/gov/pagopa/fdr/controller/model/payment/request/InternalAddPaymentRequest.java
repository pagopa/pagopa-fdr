package it.gov.pagopa.fdr.controller.model.payment.request;

import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class InternalAddPaymentRequest {

  @NotNull
  @Size(min = 1, max = AppConstant.MAX_PAYMENT_INTERNAL)
  @Valid
  @Schema(
          description =
              "The list of payments to be added on the draft flow (internal use). Date-time fields inside each payment are defined with UTC time-zone unless otherwise specified at field level.")
  private List<Payment> payments;
}

