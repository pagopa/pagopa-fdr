package it.gov.pagopa.fdr.rest.reportingFlow.response;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
public class GetAllResponse {

  @Schema(example = "1")
  private int tot;

  private List<GetResponse> reportingFlows;
}
