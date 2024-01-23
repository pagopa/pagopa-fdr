package it.gov.pagopa.fdr.service.conversion;

import com.azure.core.util.BinaryData;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.QueueMessageEncoding;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.service.conversion.message.FdrMessage;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ConversionService {

  @ConfigProperty(name = "queue.conversion.connect-str")
  String connectStr;

  @ConfigProperty(name = "queue.conversion.name")
  String queueName;

  private final Logger log;

  private QueueClient queue;

  private final ObjectMapper objectMapper;

  public ConversionService(Logger log, ObjectMapper objectMapper) {
    this.log = log;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public void init() {
    QueueClient queueClient =
        new QueueClientBuilder()
            .connectionString(connectStr)
            .queueName(queueName)
            .messageEncoding(QueueMessageEncoding.BASE64)
            .buildClient();
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
      String b64String =
          Base64.getEncoder().encodeToString(rawString.getBytes(StandardCharsets.UTF_8));
      this.queue.sendMessage(BinaryData.fromString(b64String));
    }
  }
}
