package it.gov.pagopa.fdr.controller.model.flow.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.controller.model.flow.FlowByCICreated;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"metadata", "count", "data"})
public class PaginatedFlowsCreatedResponse extends PaginatedResponse {

  @Schema(description = "The list of flows that are included in this page.")
  private List<FlowByCICreated> data;
}
