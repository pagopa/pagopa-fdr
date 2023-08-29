package it.gov.pagopa.fdr.service.conversion;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.service.conversion.message.FdrMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
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
    queueClient.createIfNotExists();

    log.infof("Queue conversion init. Queue name [%s]", queueName);
    this.queue = queueClient;
  }

  @SneakyThrows
  public void addQueueFlowMessage(FdrMessage fdrMessage) {
    if (this.queue == null) {
      log.debugf(
          "Queue conversion NOT INITIALIZED. Queue name [%s], pspId [%s], fdr [%s] NOT SENDED",
          queueName, fdrMessage.getPspId(), fdrMessage.getFdr());
    } else {
      log.infof(
          "Send message. Queue name [%s], pspId [%s], fdr [%s]",
          queueName, fdrMessage.getPspId(), fdrMessage.getFdr());
      String rawString = objectMapper.writeValueAsString(fdrMessage);
      byte[] bytes = rawString.getBytes(StandardCharsets.UTF_8);
      String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
      this.queue.sendMessage(utf8EncodedString);
    }
  }
}
