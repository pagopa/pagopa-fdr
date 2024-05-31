package it.gov.pagopa.fdr.service.reportedIuv;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.reportedIuv.model.ReportedIuv;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportedIuvService {

  private final Logger log;

  @ConfigProperty(name = "ehub.reportediuv.connect-str")
  String eHubConnectStr;

  @ConfigProperty(name = "ehub.reportediuv.name")
  String eHubName;

  private EventHubProducerClient producer;

  private final ObjectMapper objectMapper;

  public ReportedIuvService(Logger log, ObjectMapper objectMapper) {
    this.log = log;
    this.objectMapper = objectMapper;
  }

  public void init() {
    log.infof("EventHub reportediuv init. EventHub name [%s]", eHubName);

    this.producer =
        new EventHubClientBuilder()
            .connectionString(eHubConnectStr, eHubName)
            .buildProducerClient();
  }

  public final void sendEvent(ReportedIuv... list) {
    if (this.producer == null) {
      log.debugf("EventHub re [%s] NOT INITIALIZED", eHubName);
    } else {
      List<EventData> allEvents =
          Arrays.stream(list)
              .map(
                  l -> {
                    l.setUniqueId(UUID.randomUUID().toString());
                    try {
                      log.debugf("EventHub name [%s] send message: %s", eHubName, l.toString());
                      return new EventData(objectMapper.writeValueAsString(l));
                    } catch (JsonProcessingException e) {
                      log.errorf("Producer SDK Azure RE event error", e);
                      throw new AppException(
                          AppErrorCodeMessageEnum.EVENT_HUB_REPORTEDIUV_PARSE_JSON);
                    }
                  })
              .toList();
      if (!allEvents.isEmpty()) {
        publishEvents(allEvents);
      }
    }
  }

  public void publishEvents(List<EventData> allEvents) {
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
              AppErrorCodeMessageEnum.EVENT_HUB_REPORTEDIUV_TOO_LARGE,
              eventDataBatch.getMaxSizeInBytes());
        }
      }
    }
    // send the last batch of remaining events
    if (eventDataBatch.getCount() > 0) {
      producer.send(eventDataBatch);
    }
  }
}
