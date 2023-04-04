package it.gov.pagopa.fdr.rest.upload;

import it.gov.pagopa.fdr.rest.upload.request.UploadRequest;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class MultiRequest {
  @Schema(format = "binary", description = "image file")
  public String file;

  @Schema(description = "image metadata")
  public UploadRequest metadata;
}
