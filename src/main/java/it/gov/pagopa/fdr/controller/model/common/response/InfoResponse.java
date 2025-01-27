package it.gov.pagopa.fdr.controller.model.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"name", "version", "environment", "description", "errorCodes"})
public class InfoResponse {

  @Schema(example = "pagopa-fdr")
  private String name;

  @Schema(example = "1.2.3")
  private String version;

  @Schema(example = "dev")
  private String environment;

  @Schema(example = "FDR - Flussi di rendicontazione")
  private String description;
}
