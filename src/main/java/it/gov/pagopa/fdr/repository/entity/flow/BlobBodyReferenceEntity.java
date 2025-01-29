package it.gov.pagopa.fdr.repository.entity.flow;

import it.gov.pagopa.fdr.repository.enums.JsonSchemaVersionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlobBodyReferenceEntity {

  @BsonProperty("storage_account")
  private String storageAccount;

  @BsonProperty("container_name")
  private String containerName;

  @BsonProperty("file_name")
  private String fileName;

  @BsonProperty("file_length")
  private long fileLength;

  @BsonProperty("json_schema_version")
  private JsonSchemaVersionEnum jsonSchemaVersion;
}
