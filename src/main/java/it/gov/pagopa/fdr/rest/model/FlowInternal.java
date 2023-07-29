package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.fdr.util.AppConstant;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
public class FlowInternal {

  @Schema(example = "AAABBB")
  @JsonProperty(AppConstant.FDR)
  private String name;

  @Schema(example = "1")
  @JsonProperty(AppConstant.PSP)
  private String pspId;

  @Schema(example = "1")
  private Long revision;
}
