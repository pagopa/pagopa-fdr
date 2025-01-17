package it.gov.pagopa.fdr.controller.model.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorCode {

  @Schema(example = "FDR-0500")
  private String code;

  @Schema(example = "An unexpected error has occurred. Please contact support.")
  private String description;

  @Schema(example = "500")
  private int statusCode;
}
