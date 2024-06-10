package it.gov.pagopa.fdr.service.reportedIuv;

import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.gov.pagopa.fdr.service.flowTx.FlowTxService;
import it.gov.pagopa.fdr.service.reportedIuv.model.ReportedIuv;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import it.gov.pagopa.fdr.util.EventHub;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(AzuriteResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportedIuvServiceTest {

  EventHub eventHubMock;

  EventHubProducerClient producerMock;

  EventDataBatch eventDataBatchMock;

  private final ObjectMapper objectMapper;
  @InjectMock ReportedIuvService reportedIuvServiceMock;
  //  static EventHub eventHubMock;
  static List<ReportedIuv> reportedIuv;
  Field eventHubField;
  Field objectMapperField;

  ReportedIuvServiceTest(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @BeforeAll
  void init() throws NoSuchFieldException, IllegalAccessException {
    Field logField = ReportedIuvService.class.getDeclaredField("log");
    logField.setAccessible(true);
    logField.set(reportedIuvServiceMock, Logger.getLogger(ReportedIuvService.class));

    Field eHubConnectStrField = ReportedIuvService.class.getDeclaredField("eHubConnectStr");
    eHubConnectStrField.setAccessible(true);
    eHubConnectStrField.set(reportedIuvServiceMock, "eHubConnectStr");

    Field eHubNameField = ReportedIuvService.class.getDeclaredField("eHubName");
    eHubNameField.setAccessible(true);
    eHubNameField.set(reportedIuvServiceMock, "eHubName");

    objectMapperField = ReportedIuvService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);

    eventHubMock = Mockito.mock(EventHub.class);
    producerMock = Mockito.mock(EventHubProducerClient.class);
    eventDataBatchMock = Mockito.mock(EventDataBatch.class);

    Field logEventHubField = EventHub.class.getDeclaredField("log");
    logEventHubField.setAccessible(true);
    logEventHubField.set(eventHubMock, Logger.getLogger(ReportedIuvService.class));

    Field objectMapperEventHubField = EventHub.class.getDeclaredField("objectMapper");
    objectMapperEventHubField.setAccessible(true);
    objectMapperEventHubField.set(eventHubMock, objectMapper);

    Field eHubNameEventHubField = EventHub.class.getDeclaredField("eHubName");
    eHubNameEventHubField.setAccessible(true);
    eHubNameEventHubField.set(eventHubMock, "fakeName");

    Field producerEventHubField = EventHub.class.getDeclaredField("producer");
    producerEventHubField.setAccessible(true);
    producerEventHubField.set(eventHubMock, null);

    Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatchMock);
    Mockito.doNothing().when(producerMock).send(Mockito.any(EventDataBatch.class));

    Field eventHubField = FlowTxService.class.getDeclaredField("eventHub");
    eventHubField.setAccessible(true);
    eventHubField.set(reportedIuvServiceMock, eventHubMock);
  }

  @BeforeEach
  void setReInterface() throws IllegalAccessException {
    //    eventHubField.set(reportedIuvServiceMock, eventHubMock);
    objectMapperField.set(reportedIuvServiceMock, objectMapper);
    reportedIuv =
        List.of(
            ReportedIuv.builder()
                .identificativoUnivocoVersamento("")
                .identificativoUnivocoRiscossione("")
                .singoloImportoPagato(BigDecimal.valueOf(10))
                .codiceEsitoSingoloPagamento(0)
                .dataEsitoSingoloPagamento(Instant.now())
                .indiceDatiSingoloPagamento("")
                .identificativoFlusso("")
                .dataOraFlusso(Instant.now())
                .identificativoDominio("")
                .identificativoPSP("")
                .identificativoIntermediarioPSP("")
                .uniqueId(UUID.randomUUID().toString())
                .insertedTimestamp(Instant.now())
                .build());
    Mockito.clearInvocations(reportedIuvServiceMock);
    //    Mockito.clearInvocations(eventHubMock);

    Mockito.doNothing().when(reportedIuvServiceMock).init();
    Mockito.doCallRealMethod().when(reportedIuvServiceMock).sendEvent(Mockito.anyList());
  }

  @Test
  void testSendJsonProcessingException() throws JsonProcessingException, IllegalAccessException {
    ObjectMapper objectMapperMock = Mockito.mock(ObjectMapper.class);
    objectMapperField.set(reportedIuvServiceMock, objectMapperMock);
    Mockito.when(objectMapperMock.writeValueAsString(Mockito.any()))
        .thenThrow(JsonProcessingException.class);

    //      Assert.assertThrows(AppException.class, () ->
    // reportedIuvServiceMock.sendEvent(reportedIuv));
  }

  @Test
  void testSendEventNull() {
    reportedIuvServiceMock.sendEvent(null);
  }

  @Test
  void testSendEvent() {
    reportedIuvServiceMock.sendEvent(reportedIuv);
  }
}
