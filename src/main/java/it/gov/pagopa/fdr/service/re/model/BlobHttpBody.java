package it.gov.pagopa.fdr.service.re.model;

import lombok.Builder;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Builder
public class BlobHttpBody {

  @BsonProperty("storage_account")
  private String storageAccount;

  @BsonProperty("container_name")
  private String containerName;

  @BsonProperty("file_name")
  private String fileName;

  @BsonProperty("file_length")
  private long fileLength;
}
