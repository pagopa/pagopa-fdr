package it.gov.pagopa.fdr.service.re.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlobHttpBody {

  private String storageAccount;
  private String containerName;
  private String fileName;

  private long fileLength;
}
