package it.gov.pagopa.fdr.service.upload.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowDto {

  public Instant received;
  public String idFlow;
  public String fileName;
  public long fileSize;

  public String path;

  public long numberOfChunk;

  public long totChunk;

  public FlowDtoStatusEnum status;
}
