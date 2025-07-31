package it.gov.pagopa.fdr.service;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import it.gov.pagopa.fdr.repository.entity.re.ReEventEntity;
import it.gov.pagopa.fdr.service.middleware.mapper.ReEventMapper;
import it.gov.pagopa.fdr.service.model.re.BlobHttpBody;
import it.gov.pagopa.fdr.service.model.re.ReEvent;
import it.gov.pagopa.fdr.util.common.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReService {

  private final Logger log;

  @ConfigProperty(name = "blob.re.connect-str")
  String blobConnectStr;

  @ConfigProperty(name = "blob.re.name")
  String blobContainerName;

  private final BlobContainerAsyncClient blobContainerClient;

  private final ReEventMapper reEventMapper;

  public ReService(
      Logger log, ReEventMapper reEventMapper, BlobContainerAsyncClient blobContainerClient) {

    this.log = log;
    this.reEventMapper = reEventMapper;
    this.blobContainerClient = blobContainerClient;
  }

  public void init() {

    /*log.infof("RE Blob service init, container name [%s]", blobContainerName);
    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnectStr).buildClient();
    this.blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(blobContainerName);*/
  }

  public void sendEvent(ReEvent... reEvents) {
    Uni.createFrom()
        .voidItem()
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool()) // esegue storeEvents su worker
        .onItem()
        .invoke(() -> storeEvents(reEvents)) // codice sincrono, ma non blocca thread reactive
        .subscribe()
        .with(
            ignored -> {}, // ok
            failure -> log.error("Errore durante l'invio evento", failure));
  }

  private void storeEvents(ReEvent... reEvents) {

    if (this.blobContainerClient == null) {
      log.debugf("RE Blob container [%s] NOT INITIALIZED", blobContainerName);

    } else {

      for (ReEvent reEvent : reEvents) {

        try {

          // Store request and response payload as BLOB file
          writeBlobsIfExist(reEvent);

          // Store RE event in collection
          ReEventEntity reEventEntity = reEventMapper.toEntity(reEvent);
          reEventEntity.persist();

        } catch (Exception e) {
          log.errorf("An error occurred while storing events for Registro Eventi.", e);
        }
      }
    }
  }

  private void writeBlobsIfExist(ReEvent reEvent) {

    // Store request payload as BLOB file
    String reqPayload = reEvent.getReqPayload();
    if (reqPayload != null && !reqPayload.isBlank()) {
      BlobHttpBody reqBlobRef =
          writeBlob(reEvent.getSessionId(), reEvent.getFdrAction().name(), "REQ", reqPayload);
      reEvent.setReqBodyRef(reqBlobRef);
    }

    // Store response payload as BLOB file
    String resPayload = reEvent.getResPayload();
    if (resPayload != null && !resPayload.isBlank()) {
      BlobHttpBody resBlobRef =
          writeBlob(reEvent.getSessionId(), reEvent.getFdrAction().name(), "RES", reqPayload);
      reEvent.setResBodyRef(resBlobRef);
    }
  }

  private BlobHttpBody writeBlob(
      String sessionId, String fdrAction, String httpType, String payload) {

    // Construct BLOB file name
    BlobHttpBody blobHttpBody = null;
    String fileName = String.format("%s_%s_%s.json.zip", sessionId, fdrAction, httpType);

    try {

      // Compress payload as zipped content
      byte[] compressedPayload = StringUtil.zip(payload);

      // Store BLOB file on BLOB Storage
      BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(compressedPayload));
      BlobAsyncClient blobClient = blobContainerClient.getBlobAsyncClient(fileName);
      blobClient.upload(body);

      // Construct BlobHttpBody object to be returned
      blobHttpBody =
          BlobHttpBody.builder()
              .storageAccount(blobContainerClient.getAccountName())
              .containerName(blobContainerName)
              .fileName(fileName)
              .fileLength(body.getLength())
              .build();

    } catch (IOException e) {
      log.errorf(
          "An error occurred while storing events for Registro Eventi. Compression from JSON file"
              + " to BLOB file in error:",
          e);
    }

    return blobHttpBody;
  }
}
