package it.gov.pagopa.fdr.storage;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import it.gov.pagopa.fdr.util.common.FileUtil;
import it.gov.pagopa.fdr.util.common.StringUtil;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HistoryBlobStorageService {

  @ConfigProperty(name = "blob.history.connect-str")
  String blobConnectionsStr;

  @ConfigProperty(name = "blob.history.containername")
  String blobContainerName;

  private final BlobContainerClient blobContainerClient;
  private final FileUtil fileUtil;

  @Inject
  public HistoryBlobStorageService(BlobContainerClient blobContainerClient, FileUtil fileUtil) {
    this.blobContainerClient = blobContainerClient;
    this.fileUtil = fileUtil;
  }

  public void saveJsonFile(FlowBlob fdrEntity) throws IOException {
    String fileName =
        String.format(
            "%s_%s_%s.json.zip",
            fdrEntity.getFdr(), fdrEntity.getSender().getPspId(), fdrEntity.getRevision());

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    String fdrHistoryEntityJson = objectMapper.writeValueAsString(fdrEntity);

    String jsonSchema =
        fileUtil.convertToString(
            fileUtil.getFileFromResourceAsStream("/schema-json/fdr_history_schema_v1.json"));
    isJsonValid(fdrHistoryEntityJson, jsonSchema);

    byte[] compressedFdrHistoryEntityJson = StringUtil.zip(fdrHistoryEntityJson);
    BinaryData jsonFile = BinaryData.fromBytes(compressedFdrHistoryEntityJson);

    uploadBlob(fileName, jsonFile);
  }

  private void uploadBlob(String fileName, BinaryData jsonFile) {
    BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
    blobClient.upload(jsonFile, true);
  }

  public void isJsonValid(String jsonString, String jsonSchema) throws JsonProcessingException {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    JsonSchema schema = factory.getSchema(jsonSchema);
    ObjectMapper objMapper = new ObjectMapper();
    JsonNode jsonNode = objMapper.readTree(jsonString);
    Set<ValidationMessage> errors = schema.validate(jsonNode);
    if (!errors.isEmpty()) {
      throw new AppException(AppErrorCodeMessageEnum.ERROR);
    }
  }
}
