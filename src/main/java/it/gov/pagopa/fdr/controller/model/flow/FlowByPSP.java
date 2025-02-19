package it.gov.pagopa.fdr.controller.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@JsonPropertyOrder({"fdr", "pspId", "revision", "published"})
public class FlowByPSP {

  @Schema(
      example = "2025-01-0188888888888-0000001",
      description = "The unique identifier of the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_FDR)
  private String fdr;

  @Schema(
      example = "88888888888",
      description = "The domain identifier of the PSP related to the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_PSP)
  private String pspId;

  @Schema(example = "1", description = "The revision (or version) of the flow.")
  @JsonProperty(ControllerConstants.PARAMETER_REVISION)
  private Long revision;

  @Schema(
      example = "2025-01-01T12:00:30.900000Z",
      description = "The date and time on which the flow is published.")
  private Instant published;
}
