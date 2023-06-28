package it.gov.pagopa.fdr.service.re;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.re.model.ReAbstract;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

  private EventHubProducerClient producer;

  @Inject ObjectMapper objectMapper;

  public void init() {
    EventHubProducerClient producer =
        new EventHubClientBuilder()
            //            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .connectionString(eHubConnectStr, eHubName)
            .buildProducerClient();

    log.infof("EventHub re init. EventHub name [%s]", eHubName);
    this.producer = producer;
  }

  public <T extends ReAbstract> void sendEvent(T... reList) {
    List<EventData> allEvents =
        Arrays.stream(reList)
            .map(
                re -> {
                  try {
                    log.info(re.toString());
                    return new EventData(objectMapper.writeValueAsString("Foo"));
                  } catch (JsonProcessingException e) {
                    log.errorf("Producer SDK Azure RE event error", e);
                    throw new AppException(AppErrorCodeMessageEnum.EVENT_HUB_RE_PARSE_JSON);
                  }
                })
            .toList();

    publishEvents(allEvents);
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
    //    producer.close();
  }
}
