package it.gov.pagopa.fdr.controller.psps.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.controller.model.FdrInserted;
import it.gov.pagopa.fdr.controller.model.Metadata;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"metadata", "count", "data"})
public class GetAllCreatedResponse {

  private Metadata metadata;

  @Schema(example = "100")
  private long count;

  private List<FdrInserted> data;
}
