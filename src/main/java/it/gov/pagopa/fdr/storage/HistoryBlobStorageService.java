package it.gov.pagopa.fdr.storage;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import it.gov.pagopa.fdr.util.common.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HistoryBlobStorageService {

  @ConfigProperty(name = "blob.history.connect-str")
  String blobConnectionsStr;

  @ConfigProperty(name = "blob.history.containername")
  String blobContainerName;

  private final BlobContainerClient blobContainerClient;

  @Inject
  public HistoryBlobStorageService(BlobContainerClient blobContainerClient) {
    this.blobContainerClient = blobContainerClient;
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
    byte[] compressedFdrHistoryEntityJson = StringUtil.zip(fdrHistoryEntityJson);
    BinaryData jsonFile = BinaryData.fromBytes(compressedFdrHistoryEntityJson);

    uploadBlob(fileName, jsonFile);
  }

  private void uploadBlob(String fileName, BinaryData jsonFile) {
    BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
    blobClient.upload(jsonFile, true);
  }
}
