package it.gov.pagopa.fdr.service.queue;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.service.queue.message.FlowMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ConversionQueue {

  @ConfigProperty(name = "queue.conversion.connect-str")
  String connectStr;

  @ConfigProperty(name = "queue.conversion.name")
  String queueName;

  @Inject Logger log;

  private QueueClient queue;

  @Inject ObjectMapper objectMapper;

  @SneakyThrows
  public void init() {
    QueueClient queue =
        new QueueClientBuilder().connectionString(connectStr).queueName(queueName).buildClient();

    log.debugf("Queue conversion init. Queue name [%s]", queueName);
    this.queue = queue;
  }

  @SneakyThrows
  public void addQueueFlowMessage(FlowMessage flowMessage) {
    if (this.queue == null) {
      log.debugf(
          "Queue conversion NOT INITIALIZED. Queue name [%s], pspId [%s], flowName [%s] NOT SENDED",
          queueName, flowMessage.getPspId(), flowMessage.getName());
    } else {
      log.debugf(
          "Send message. Queue name [%s], pspId [%s], flowName [%s]",
          queueName, flowMessage.getPspId(), flowMessage.getName());
      this.queue.sendMessage(objectMapper.writeValueAsString(flowMessage));
    }
  }
}
