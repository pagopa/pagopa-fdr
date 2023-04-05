package it.gov.pagopa.fdr.rest.upload.request;

import it.gov.pagopa.fdr.util.CheckInstantFormat;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
public abstract class AbstractUpload {

  @NotNull(message = "upload.date.not-null")
  @Schema(example = "2023-04-05T09:21:37.810533Z")
  @CheckInstantFormat(message = "upload.date.format|${validatedValue}|{pattern}")
  private String date;

  @NotNull(message = "upload.idPsp.not-null")
  @Schema(example = "60000000001")
  private String idPsp;

  @NotNull(message = "upload.id-flow.not-null")
  @Schema(example = "1173")
  private String idFlow;
}
