package it.gov.pagopa.fdr.controller.model.payment.request;

import it.gov.pagopa.fdr.util.constant.AppConstant;
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
public class DeletePaymentRequest {

  @NotNull
  @Size(min = 1, max = AppConstant.MAX_PAYMENT)
  @Schema(description = "The list of payments indexes to be removed on the draft flow")
  private List<Long> indexList;
}
