package it.gov.pagopa.fdr.service.history;

import com.azure.core.util.BinaryData;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.service.history.constants.HistoryConstants;
import it.gov.pagopa.fdr.service.history.mapper.HistoryServiceMapper;
import it.gov.pagopa.fdr.service.history.model.FdrHistoryEntity;
import it.gov.pagopa.fdr.service.history.model.FdrHistoryPaymentEntity;
import it.gov.pagopa.fdr.service.history.model.HistoryBlobBody;
import it.gov.pagopa.fdr.service.history.model.JsonSchemaVersionEnum;
import it.gov.pagopa.fdr.util.FileUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HistoryService {
  private final HistoryServiceMapper mapper;
  private final Logger logger;
  private final ObjectMapper objMapper;

  private final FileUtil fileUtil;

  @ConfigProperty(name = "blob.history.connect-str")
  String blobConnectionsStr;

  @ConfigProperty(name = "blob.history.containername")
  String blobContainerName;

  @ConfigProperty(name = "table.history.connect-str")
  String tableStorageConnString;

  @ConfigProperty(name = "table.history.tablename.fdrpublish")
  String tableNameFdrPublish;

  @ConfigProperty(name = "table.history.tablename.fdrpaymentpublish")
  String tableNameFdrPaymentPublish;

  @ConfigProperty(name = "json.schema.version")
  String jsonSchemaVersion;

  private BlobContainerClient blobContainerClient;
  private TableServiceClient tableServiceClient;
  private String jsonSchema;

  public HistoryService(
      HistoryServiceMapper mapper, Logger logger, ObjectMapper objMapper, FileUtil fileUtil) {
    this.mapper = mapper;
    this.logger = logger;
    this.objMapper = objMapper;
    this.fileUtil = fileUtil;
  }

  public void init() {
    logger.infof(
        "Blob Storage HistoryService service init. Container name [%s]", blobContainerName);
    BlobServiceClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobConnectionsStr).buildClient();
    this.blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(blobContainerName);
    this.tableServiceClient =
        new TableServiceClientBuilder().connectionString(tableStorageConnString).buildClient();
    this.tableServiceClient.createTableIfNotExists(tableNameFdrPublish);
    this.tableServiceClient.createTableIfNotExists(tableNameFdrPaymentPublish);
    this.jsonSchema =
        fileUtil.convertToString(
            fileUtil.getFileFromResourceAsStream(
                "/schema-json/fdr_history_schema_" + jsonSchemaVersion.toLowerCase() + ".json"));
  }

  public void saveOnStorage(
      FdrPublishEntity fdrEntity, List<FdrPaymentPublishEntity> paymentsList) {
    if (blobContainerClient != null) {
      try {
        String partitionKey = createPartitionKey(fdrEntity.getPublished());
        saveFdrOnTableStorage(fdrEntity, partitionKey);
        saveFdrPaymentsOnTableStorage(paymentsList, partitionKey);
      } catch (Exception e) {
        logger.error("Error while uploading FDR History on Table Storage", e);
        throw new AppException(AppErrorCodeMessageEnum.FDR_HISTORY_SAVE_TABLE_STORAGE_ERROR);
      }
    } else {
      logger.debugf("Blob container [%s] NOT INITIALIZED", blobContainerName);
    }
  }

  public HistoryBlobBody saveJsonFile(
      FdrPublishEntity fdrEntity, List<FdrPaymentPublishEntity> paymentsList) {
    if (blobContainerClient != null) {
      FdrHistoryEntity fdrHistoryEntity = mapper.toFdrHistoryEntity(fdrEntity);
      List<FdrHistoryPaymentEntity> fdrHistoryPaymentEntityList =
          mapper.toFdrHistoryPaymentEntityList(paymentsList);
      fdrHistoryEntity.setPaymentList(fdrHistoryPaymentEntityList);
      String fileName =
          String.format(
              "%s_%s_%s.json",
              fdrEntity.getFdr(), fdrEntity.getSender().getPspId(), fdrEntity.getRevision());

      try {
        String fdrHistoryEntityJson = objMapper.writeValueAsString(fdrHistoryEntity);

        isJsonValid(fdrHistoryEntityJson, jsonSchema);
        BinaryData jsonFile = BinaryData.fromString(fdrHistoryEntityJson);

        uploadBlob(fileName, jsonFile);
        return HistoryBlobBody.builder()
            .storageAccount(blobContainerClient.getAccountName())
            .containerName(blobContainerName)
            .fileName(fileName)
            .fileLength(jsonFile.getLength())
            .jsonSchemaVersion(JsonSchemaVersionEnum.valueOf(jsonSchemaVersion))
            .build();
      } catch (JsonProcessingException e) {
        logger.error("Error processing fdrHistoryEntity", e);
        throw new AppException(AppErrorCodeMessageEnum.FDR_HISTORY_JSON_PROCESSING_ERROR);
      }
    } else {
      logger.debugf("Blob container [%s] NOT INITIALIZED", blobContainerName);
      return null;
    }
  }

  private void uploadBlob(String fileName, BinaryData jsonFile) {
    try {
      BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
      blobClient.upload(jsonFile);
    } catch (Exception e) {
      logger.error("Error uploading history JSON blob", e);
      throw new AppException(AppErrorCodeMessageEnum.FDR_HISTORY_UPLOAD_JSON_BLOB_ERROR);
    }
  }

  public void isJsonValid(String jsonString, String jsonSchema) throws JsonProcessingException {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    JsonSchema schema = factory.getSchema(jsonSchema);
    JsonNode jsonNode = objMapper.readTree(jsonString);
    Set<ValidationMessage> errors = schema.validate(jsonNode);
    if (!errors.isEmpty()) {
      logger.errorf(
          "History JSON validation failed. [%s]",
          errors.stream().map(e -> e.getMessage()).collect(Collectors.joining(", ")));
      throw new AppException(AppErrorCodeMessageEnum.FDR_HISTORY_VALID_JSON_ERROR);
    }
  }

  private String createPartitionKey(Instant publishTime) {
    return publishTime.toString().substring(0, 10);
  }

  private void saveFdrOnTableStorage(FdrPublishEntity fdrPublishEntity, String partitionKey) {
    Map<String, Object> fdrPublishMap = new LinkedHashMap<>();
    String id = String.valueOf(fdrPublishEntity.id);
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_ID, id);
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_REVISION, fdrPublishEntity.getRevision());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_CREATED, fdrPublishEntity.getCreated());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_UPDATED, fdrPublishEntity.getUpdated());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_PUBLISHED, fdrPublishEntity.getPublished());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_FDR, fdrPublishEntity.getFdr());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_FDR_DATE, fdrPublishEntity.getFdrDate());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_FDR_REF_JSON_CONTAINER_NAME,
        fdrPublishEntity.getRefJson().getContainerName());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_FDR_REF_JSON_FILE_LENGTH,
        fdrPublishEntity.getRefJson().getFileLength());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_FDR_REF_JSON_FILE_NAME,
        fdrPublishEntity.getRefJson().getFileName());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_FDR_REF_JSON_STORAGE_ACCOUNT,
        fdrPublishEntity.getRefJson().getStorageAccount());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_FDR_REF_JSON_JSON_SCHEMA_VERSION,
        fdrPublishEntity.getRefJson().getJsonSchemaVersion());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_SENDER_TYPE, fdrPublishEntity.getSender().getType());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_SENDER_ID, fdrPublishEntity.getSender().getId());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_SENDER_PSP_ID, fdrPublishEntity.getSender().getPspId());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_SENDER_PSP_NAME, fdrPublishEntity.getSender().getPspName());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_SENDER_PSP_BROKER_ID,
        fdrPublishEntity.getSender().getPspBrokerId());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_SENDER_CHANNEL_ID,
        fdrPublishEntity.getSender().getChannelId());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_SENDER_PASSWORD, fdrPublishEntity.getSender().getPassword());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_RECEIVER_ID, fdrPublishEntity.getReceiver().getId());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_RECEIVER_ORGANIZATION_ID,
        fdrPublishEntity.getReceiver().getOrganizationId());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_RECEIVER_ORGANIZATION_NAME,
        fdrPublishEntity.getReceiver().getOrganizationName());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_REGULATION, fdrPublishEntity.getRegulation());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_REGULATION_DATE, fdrPublishEntity.getRegulationDate());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_BIC_CODE_POURING_BANK,
        fdrPublishEntity.getBicCodePouringBank());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_STATUS, fdrPublishEntity.getStatus());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_COMPUTED_TOT_PAYMENTS,
        fdrPublishEntity.getComputedTotPayments());
    fdrPublishMap.put(
        HistoryConstants.FDR_PUBLISH_COMPUTED_SUM_PAYMENTS,
        fdrPublishEntity.getComputedSumPayments());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_TOT_PAYMENTS, fdrPublishEntity.getTotPayments());
    fdrPublishMap.put(HistoryConstants.FDR_PUBLISH_SUM_PAYMENTS, fdrPublishEntity.getSumPayments());

    logger.info(
        "Send to "
            + tableNameFdrPublish
            + " record with "
            + HistoryConstants.FDR_PUBLISH_ID
            + "="
            + id);
    TableClient tableClient = this.tableServiceClient.getTableClient(tableNameFdrPublish);
    TableEntity entity = new TableEntity(partitionKey, id);
    entity.setProperties(fdrPublishMap);
    tableClient.createEntity(entity);
  }

  private void saveFdrPaymentsOnTableStorage(
      List<FdrPaymentPublishEntity> paymentsList, String partitionKey) {
    paymentsList.forEach(
        payment -> {
          Map<String, Object> paymentMap = new LinkedHashMap<>();
          String id = String.valueOf(payment.id);
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_ID, id);
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_REVISION, payment.getRevision());
          paymentMap.put(
              HistoryConstants.FDR_PAYMENT_PUBLISH_REF_FDR_ID,
              String.valueOf(payment.getRefFdrId()));
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_CREATED, payment.getCreated());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_UPDATED, payment.getUpdated());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_IUV, payment.getIuv());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_IUR, payment.getIur());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_INDEX, payment.getIndex());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_PAY, payment.getPay());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_PAY_STATUS, payment.getPayStatus());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_PAY_DATE, payment.getPayDate());
          paymentMap.put(HistoryConstants.FDR_PAYMENT_PUBLISH_REF_FDR, payment.getRefFdr());
          paymentMap.put(
              HistoryConstants.FDR_PAYMENT_PUBLISH_REF_FDR_SENDER_PSP_ID,
              payment.getRefFdrSenderPspId());
          paymentMap.put(
              HistoryConstants.FDR_PAYMENT_PUBLISH_REF_FDR_REVISION, payment.getRefFdrRevision());
          paymentMap.put(
              HistoryConstants.FDR_PAYMENT_PUBLISH_REF_FDR_RECEIVER_ORGANIZATION_ID,
              payment.getRefFdrReceiverOrganizationId());

          logger.info(
              "Send to "
                  + tableNameFdrPaymentPublish
                  + " record with "
                  + HistoryConstants.FDR_PAYMENT_PUBLISH_REF_FDR_ID
                  + "="
                  + id);
          TableClient tableClient =
              this.tableServiceClient.getTableClient(tableNameFdrPaymentPublish);
          TableEntity entity = new TableEntity(partitionKey, id);
          entity.setProperties(paymentMap);
          tableClient.createEntity(entity);
        });
  }
}
