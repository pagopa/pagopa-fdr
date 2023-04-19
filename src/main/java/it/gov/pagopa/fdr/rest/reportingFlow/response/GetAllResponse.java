package it.gov.pagopa.fdr.rest.reportingFlow.response;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Metadata;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
public class GetAllResponse {

  private Metadata metadata;

  @Schema(example = "100")
  private long count;

  private List<String> data;
}
