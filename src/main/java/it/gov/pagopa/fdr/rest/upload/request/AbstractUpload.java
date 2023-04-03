package it.gov.pagopa.fdr.rest.upload.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class AbstractUpload {

  @Size(min = 2, max = 35, message = "upload.id-flow.length.size|${validatedValue}|{min}|{max}")
  @NotNull(message = "upload.id-flow.not-null")
  private String idFlow;
}
