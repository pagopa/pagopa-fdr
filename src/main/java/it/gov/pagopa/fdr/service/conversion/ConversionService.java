package it.gov.pagopa.fdr.service.conversion;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.service.conversion.message.FlowMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ConversionService {

  @ConfigProperty(name = "queue.conversion.connect-str")
  String connectStr;

  @ConfigProperty(name = "queue.conversion.name")
  String queueName;

  @Inject Logger log;

  private QueueClient queue;

  @Inject ObjectMapper objectMapper;

  @SneakyThrows
  public void init() {
    QueueClient queueClient =
        new QueueClientBuilder().connectionString(connectStr).queueName(queueName).buildClient();
    queueClient.create();

    log.infof("Queue conversion init. Queue name [%s]", queueName);
    this.queue = queueClient;
  }

  @SneakyThrows
  public void addQueueFlowMessage(FlowMessage flowMessage) {
    if (this.queue == null) {
      log.debugf(
          "Queue conversion NOT INITIALIZED. Queue name [%s], pspId [%s], flowName [%s] NOT SENDED",
          queueName, flowMessage.getPspId(), flowMessage.getName());
    } else {
      log.infof(
          "Send message. Queue name [%s], pspId [%s], flowName [%s]",
          queueName, flowMessage.getPspId(), flowMessage.getName());
      this.queue.sendMessage(objectMapper.writeValueAsString(flowMessage));
    }
  }
}
