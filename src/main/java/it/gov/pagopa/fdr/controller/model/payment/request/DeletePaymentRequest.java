package it.gov.pagopa.fdr.controller.model.payment.request;

import it.gov.pagopa.fdr.util.constant.AppConstant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DeletePaymentRequest {

  @NotNull
  @Size(min = 1, max = AppConstant.MAX_PAYMENT)
  private List<Long> indexList;
}
