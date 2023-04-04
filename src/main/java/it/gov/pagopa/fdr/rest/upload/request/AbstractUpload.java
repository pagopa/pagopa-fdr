package it.gov.pagopa.fdr.rest.upload.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
public abstract class AbstractUpload {

  @Size(min = 2, max = 35, message = "upload.id-flow.length.size|${validatedValue}|{min}|{max}")
  @NotNull(message = "upload.id-flow.not-null")
  @Schema(example = "2023-01-3060000000001-1173")
  private String idFlow;
}
