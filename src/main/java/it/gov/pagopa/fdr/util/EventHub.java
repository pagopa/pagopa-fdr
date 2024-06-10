package it.gov.pagopa.fdr.util;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import java.util.List;
import org.jboss.logging.Logger;

public class EventHub {

  private final Logger log;
  private final ObjectMapper objectMapper;
  private final String eHubName;
  private final EventHubProducerClient producer;

  public EventHub(Logger log, ObjectMapper objectMapper, String eHubConnectStr, String eHubName) {
    this.log = log;
    this.objectMapper = objectMapper;
    this.eHubName = eHubName;

    log.infof("EventHub init. EventHub name [%s]", eHubName);
    this.producer =
        new EventHubClientBuilder()
            .connectionString(eHubConnectStr, eHubName)
            .buildProducerClient();
  }

  public final <T> void sendEvent(List<T> list) {
    List<EventData> allEvents =
        list.stream()
            .map(
                l -> {
                  try {
                    log.debugf("EventHub name [%s] send message: %s", eHubName, l.toString());
                    return new EventData(objectMapper.writeValueAsString(l));
                  } catch (JsonProcessingException e) {
                    log.errorf("Producer SDK Azure event error", e);
                    throw new AppException(AppErrorCodeMessageEnum.EVENT_HUB_PARSE_JSON);
                  }
                })
            .toList();
    if (!allEvents.isEmpty()) {
      publishEvents(allEvents);
    }
  }

  private void publishEvents(List<EventData> allEvents) {
    // create a batch
    EventDataBatch eventDataBatch = this.producer.createBatch();

    for (EventData eventData : allEvents) {
      // try to add the event from the array to the batch
      if (!eventDataBatch.tryAdd(eventData)) {
        // if the batch is full, send it and then create a new batch
        producer.send(eventDataBatch);
        eventDataBatch = producer.createBatch();

        // Try to add that event that couldn't fit before.
        if (!eventDataBatch.tryAdd(eventData)) {
          throw new AppException(
              AppErrorCodeMessageEnum.EVENT_HUB_TOO_LARGE, eventDataBatch.getMaxSizeInBytes());
        }
      }
    }
    // send the last batch of remaining events
    if (eventDataBatch.getCount() > 0) {
      producer.send(eventDataBatch);
    }
  }
}
