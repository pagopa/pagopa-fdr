package it.gov.pagopa.fdr.service.model.re;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
