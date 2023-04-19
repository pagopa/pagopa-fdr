package it.gov.pagopa.fdr.rest.reportingFlow.response;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Metadata;
import it.gov.pagopa.fdr.rest.reportingFlow.model.Pagamento;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
public class GetPaymentResponse {

  private Metadata metadata;

  @Schema(example = "100")
  private long count;

  private List<Pagamento> data;
}
