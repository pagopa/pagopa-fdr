package it.gov.pagopa.fdr.controller.model.flow.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.controller.model.flow.FlowBySenderAndReceiver;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"metadata", "count", "data"})
public class PaginatedFlowsBySenderAndReceiverResponse extends PaginatedResponse {

  private List<FlowBySenderAndReceiver> data;
}
