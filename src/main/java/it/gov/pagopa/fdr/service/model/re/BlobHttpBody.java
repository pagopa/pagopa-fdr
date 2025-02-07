package it.gov.pagopa.fdr.service.model.re;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlobHttpBody {

  private String storageAccount;

  private String containerName;

  private String fileName;

  private long fileLength;
}
