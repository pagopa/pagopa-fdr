package it.gov.pagopa.fdr.service;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.fdr.repository.entity.re.ReEventEntity;
import it.gov.pagopa.fdr.service.middleware.mapper.ReEventMapper;
import it.gov.pagopa.fdr.service.model.re.BlobHttpBody;
import it.gov.pagopa.fdr.service.model.re.ReEvent;
import it.gov.pagopa.fdr.util.common.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReService {

  private final Logger log;

  @ConfigProperty(name = "blob.re.name")
  String blobContainerName;

  private final BlobContainerClient blobContainerClient;

  private final ReEventMapper reEventMapper;

  private final ExecutorService eventExecutor;

  public ReService(
      Logger log, ReEventMapper reEventMapper,
      BlobContainerClient blobContainerClient
  ) {

    this.log = log;
    this.reEventMapper = reEventMapper;
    this.blobContainerClient = blobContainerClient;

    this.eventExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
      private final AtomicInteger counter = new AtomicInteger(1);
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("re-event-sender-" + counter.getAndIncrement());
        return t;
      }
    });
  }

  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public void sendEvent(ReEvent... reEvents) {
    // fire and forget
    Uni.createFrom()
            .voidItem()
            .runSubscriptionOn(eventExecutor)
            .onItem()
            .invoke(() -> storeEvents(reEvents)) // codice sincrono, ma non blocca thread reactive
            .subscribe()
            .with(ignored -> {}, failure -> {});
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
          ReEventEntity entity = reEventMapper.toEntity(reEvent);
          entity.persist();
          log.debugf("RE Event [%s] saved", entity.id);
        } catch (Exception e) {
          log.errorf("Errore durante il salvataggio dell'evento %s: %s", reEvent, e.getMessage(), e);
        }
      }
    }
  }

  private void writeBlobsIfExist(ReEvent reEvent) {

    // Store request payload as BLOB file
    String reqPayload = reEvent.getReqPayload();
    if (reqPayload != null && !reqPayload.isBlank()) {
      BlobHttpBody reqBlobRef =
          writeBlob(
              reEvent.getSessionId(),
              reEvent.getCreated(),
              reEvent.getFdrAction().name(),
              "REQ",
              reqPayload);
      reEvent.setReqBodyRef(reqBlobRef);
    }

    // Store response payload as BLOB file
    String resPayload = reEvent.getResPayload();
    if (resPayload != null && !resPayload.isBlank()) {
      BlobHttpBody resBlobRef =
          writeBlob(
              reEvent.getSessionId(),
              reEvent.getCreated(),
              reEvent.getFdrAction().name(),
              "RES",
              resPayload);
      reEvent.setResBodyRef(resBlobRef);
    }
  }

  private BlobHttpBody writeBlob(
      String sessionId, Instant createdAt, String fdrAction, String httpType, String payload) {

    // Construct BLOB file name
    BlobHttpBody blobHttpBody = null;

    LocalDateTime creationDate = LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault());
    String folder =
        String.format(
            "%d/%02d/%02d/%02d",
            creationDate.getYear(),
            creationDate.getMonthValue(),
            creationDate.getDayOfMonth(),
            creationDate.getHour());
    String fileName = String.format("%s/%s_%s_%s.json.zip", folder, sessionId, fdrAction, httpType);

    try {

      // Compress payload as zipped content
      byte[] compressedPayload = StringUtil.zip(payload);

      // Store BLOB file on BLOB Storage
      // Note: Before the method BinaryData.fromStream(new ByteArrayInputStream(compressedPayload))
      // was used, but getLength() method will always return null. Currently using fromBytes() method but if
      // used memory is too much high, return to use fromStream() method.
      BinaryData body = BinaryData.fromBytes(compressedPayload);
      BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
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
