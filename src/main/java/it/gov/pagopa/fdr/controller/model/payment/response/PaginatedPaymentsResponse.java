package it.gov.pagopa.fdr.controller.model.payment.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"metadata", "count", "data"})
public class PaginatedPaymentsResponse {

  @Schema(description = "The metadata related to the paginated response.")
  private Metadata metadata;

  @Schema(example = "100", description = "The number of elements that can be found in this page.")
  private Long count;

  @Schema(description = "The list of payments that are included in this page.")
  private List<Payment> data;
}
