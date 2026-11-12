package it.gov.pagopa.fdr.storage;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.repository.enums.BlobProcessingStatusEnum;
import it.gov.pagopa.fdr.repository.enums.BlobTagNameEnum;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import it.gov.pagopa.fdr.util.common.FileUtil;
import it.gov.pagopa.fdr.util.common.StringUtil;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HistoryBlobStorageService {

  private final BlobContainerAsyncClient blobContainerClient;
  private final FileUtil fileUtil;
  private final Logger log;
  @Inject
  public HistoryBlobStorageService(
      BlobContainerAsyncClient blobContainerClient, FileUtil fileUtil,  Logger log) {
    this.blobContainerClient = blobContainerClient;
    this.fileUtil = fileUtil;
    this.log = log;
  }

  public void saveJsonFile(FlowBlob fdrEntity, FlowToHistoryEntity flowToHistoryEntity)
      throws IOException, ValidationException {
    String fileName =
        String.format(
            "%s_%s_%s.json.zip",
            fdrEntity.getFdr(), fdrEntity.getSender().getPspId(), fdrEntity.getRevision());

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    String fdrHistoryEntityJson =
        objectMapper
            .writer()
            .with(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
            .writeValueAsString(fdrEntity);

    String jsonSchema =
        fileUtil.convertToString(
            fileUtil.getFileFromResourceAsStream("/schema-json/fdr_history_schema_v1.json"));
    isJsonValid(fdrHistoryEntityJson, jsonSchema);

    byte[] compressedFdrHistoryEntityJson = StringUtil.zip(fdrHistoryEntityJson);
    BinaryData jsonFile = BinaryData.fromBytes(compressedFdrHistoryEntityJson);

    boolean permitsElaborationForQI = flowToHistoryEntity.getIsExternal();
    uploadBlob(fdrEntity, fileName, permitsElaborationForQI, jsonFile);
  }

  private void uploadBlob(
      FlowBlob fdrEntity,
      String fileName,
      boolean permitsElaborationForQI,
      BinaryData jsonFile) {

    BlobAsyncClient blobClient =
        blobContainerClient.getBlobAsyncClient(fileName);

    Map<String, String> metadata = new HashMap<>();
    metadata.put("elaborate", Boolean.toString(permitsElaborationForQI));
    metadata.put("sessionId", UUID.randomUUID().toString());
    metadata.put("insertedTimestamp", fdrEntity.getPublished().toString());
    metadata.put("serviceIdentifier", AppConstant.SERVICE_IDENTIFIER);

    Map<String, String> tags = initialTags();

    BlobParallelUploadOptions options =
        new BlobParallelUploadOptions(jsonFile)
            .setMetadata(metadata)
            .setTags(tags);

    blobClient
        .uploadWithResponse(options)
        .flatMap(response -> blobClient.getTags())
        .subscribe(
            savedTags -> log.infof(
                "Blob [%s] uploaded successfully. Saved tags: %s",
                fileName,
                savedTags
            ),
            error -> log.errorf(
                error,
                "Error uploading blob [%s]",
                fileName
            )
        );
  }

  private void isJsonValid(String jsonString, String jsonSchema)
      throws ValidationException, JsonProcessingException {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    JsonSchema schema = factory.getSchema(jsonSchema);
    ObjectMapper objMapper = new ObjectMapper();
    JsonNode jsonNode = objMapper.readTree(jsonString);
    Set<ValidationMessage> errors = schema.validate(jsonNode);
    if (!errors.isEmpty()) {
      throw new ValidationException(errors.toString());
    }
  }

  private Map<String, String> initialTags() {
    return Map.of(
        BlobTagNameEnum.PROCESSING_STATUS.getValue(),
        BlobProcessingStatusEnum.DA_ELABORARE.name(),
        BlobTagNameEnum.PROCESSING_ATTEMPT.getValue(),
        "0",
        BlobTagNameEnum.PROCESSING_UPDATED_AT.getValue(),
        Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
  }
}
