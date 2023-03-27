package it.gov.pagopa.fdr.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FruitDto {

  @Schema(description = "Name of fruit", required = true, example = "Apple")
  private String name;

  private String description;
}
