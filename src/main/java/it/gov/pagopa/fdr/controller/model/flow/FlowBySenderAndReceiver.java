package it.gov.pagopa.fdr.controller.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import java.time.Instant;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({"pspId", "organizationId", "fdr", "revision", "created"})
public class FlowBySenderAndReceiver {

  @Schema(
      example = "88888888888",
      description = "The domain identifier of the PSP related to the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_PSP)
  private String pspId;

  @Schema(
      example = "77777777777",
      description = "The domain identifier of the creditor institution related to the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_ORGANIZATION)
  private String organizationId;

  @Schema(
      example = "2025-01-0188888888888-0000001",
      description = "The unique identifier of the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_FDR)
  private String fdr;

  @Schema(example = "1", description = "The revision (or version) of the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_REVISION)
  private Long revision;

  @Schema(
      example = "2025-01-01T12:00:30.900000Z",
      description = "The date and time on which the flow is created.")
  private Instant created;
}
