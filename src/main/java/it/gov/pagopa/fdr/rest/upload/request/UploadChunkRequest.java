package it.gov.pagopa.fdr.rest.upload.request;

import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
@Schema(format = "json")
public class UploadChunkRequest extends AbstractUpload {

  @Min(value = 1, message = "upload.number-of-chunk.min|${validatedValue}|{value}")
  @Schema(example = "1")
  private int numberOfChunk;

  @Min(value = 1, message = "upload.tot-chunk.min|${validatedValue}|{value}")
  @Schema(example = "1")
  private int totChunk;
}
