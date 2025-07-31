package it.gov.pagopa.fdr.repository.entity.re;

import lombok.Data;

@Data
public class BlobRefEntity {

  private String storageAccount;

  private String containerName;

  private String fileName;

  private Long fileLength;
}
