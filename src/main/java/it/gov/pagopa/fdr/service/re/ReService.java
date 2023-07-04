package it.gov.pagopa.fdr.service.re;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.re.model.BlobHttpBody;
import it.gov.pagopa.fdr.service.re.model.ReAbstract;
import it.gov.pagopa.fdr.service.re.model.ReInterface;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReService {

  @Inject Logger log;

  @ConfigProperty(name = "ehub.re.connect-str")
  String eHubConnectStr;

  @ConfigProperty(name = "ehub.re.name")
  String eHubName;

  @ConfigProperty(name = "blob.re.connect-str")
  String blobConnectStr;

  @ConfigProperty(name = "blob.re.containername")
  String blobContainerName;

  private EventHubProducerClient producer;

  private BlobContainerClient blobContainerClient;

  @Inject ObjectMapper objectMapper;

  public void init() {
    log.infof(
        "EventHub re and blob service init. EventHub name [%s], container name [%s]",
        eHubName, blobContainerName);

    this.producer =
        new EventHubClientBuilder()
            .connectionString(eHubConnectStr, eHubName)
            .buildProducerClient();

    BlobServiceClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobConnectStr).buildClient();
    this.blobContainerClient = blobServiceClient.getBlobContainerClient(blobContainerName);
  }

  @SafeVarargs
  public final <T extends ReAbstract> void sendEvent(T... reList) {
    if (this.producer == null || this.blobContainerClient == null) {
      log.debugf(
          "EventHub re [%s] or Blob container [%s] NOT INITIALIZED", eHubName, blobContainerName);
    } else {
      List<EventData> allEvents =
          Arrays.stream(reList)
              .map(
                  re -> {
                    if (re instanceof ReInterface) {
                      writeBlob((ReInterface) re);
                    }
                    try {
                      log.debugf("EventHub name [%s] send message: %s", re.toString());
                      return new EventData(objectMapper.writeValueAsString(re));
                    } catch (JsonProcessingException e) {
                      log.errorf("Producer SDK Azure RE event error", e);
                      throw new AppException(AppErrorCodeMessageEnum.EVENT_HUB_RE_PARSE_JSON);
                    }
                  })
              .toList();

      publishEvents(allEvents);
    }
  }

  private void writeBlob(ReInterface re) {
    String fileName =
        String.format("%s_%s_%s", re.getSessionId(), re.getHttpType().name(), re.getFlowName());

    InputStream body = re.getBodyRef();

    BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
    blobClient.upload(body);

    try {
      BlobHttpBody blobBodyRef =
          BlobHttpBody.builder()
              .storageAccount(blobContainerClient.getAccountName())
              .containerName(blobContainerName)
              .fileName(fileName)
              .fileLength(body.available())
              .build();
      ((ReInterface) re).setBlobBodyRef(blobBodyRef);
      ((ReInterface) re).setBodyRef(null);
    } catch (IOException e) {
      throw new AppException(AppErrorCodeMessageEnum.BLOB_RE_ERROR);
    }
  }

  private void publishEvents(List<EventData> allEvents) {
    // create a batch
    EventDataBatch eventDataBatch = producer.createBatch();

    for (EventData eventData : allEvents) {
      // try to add the event from the array to the batch
      if (!eventDataBatch.tryAdd(eventData)) {
        // if the batch is full, send it and then create a new batch
        producer.send(eventDataBatch);
        eventDataBatch = producer.createBatch();

        // Try to add that event that couldn't fit before.
        if (!eventDataBatch.tryAdd(eventData)) {
          throw new AppException(
              AppErrorCodeMessageEnum.EVENT_HUB_RE_TOO_LARGE, eventDataBatch.getMaxSizeInBytes());
        }
      }
    }
    // send the last batch of remaining events
    if (eventDataBatch.getCount() > 0) {
      producer.send(eventDataBatch);
    }
  }
}
