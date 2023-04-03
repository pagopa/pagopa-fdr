package it.gov.pagopa.fdr.rest.upload.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadChunkResponse {
  private String idFlow;
  private Instant received;

  private int numberOfChunk = 1;
  private int totChunk = 1;
}
