package it.gov.pagopa.fdr.controller.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
public class FlowByPSP {

  @Schema(example = "AAABBB")
  @JsonProperty(ControllerConstants.PARAMETER_FDR)
  private String fdr;

  @Schema(example = "1")
  @JsonProperty(ControllerConstants.PARAMETER_PSP)
  private String pspId;

  @Schema(example = "1")
  @JsonProperty(ControllerConstants.PARAMETER_REVISION)
  private Long revision;

  @Schema(example = "2023-04-03T12:00:30.900000Z")
  private Instant published;
}
