package it.gov.pagopa.fdr.rest.psps.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.rest.model.Payment;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"metadata", "count", "sum", "data"})
public class GetPaymentCreatedResponse {

  private Metadata metadata;

  @Schema(example = "100")
  private Long count;

  private List<Payment> data;
}