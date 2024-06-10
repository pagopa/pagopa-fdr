package it.gov.pagopa.fdr.service.re;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.gov.pagopa.fdr.service.flowTx.FlowTxService;
import it.gov.pagopa.fdr.service.flowTx.model.FlowTx;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import java.lang.reflect.Field;
import java.time.Instant;
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
class FlowTxServiceTest {
  private final ObjectMapper objectMapper;
  @InjectMock FlowTxService flowTxServiceMock;
  //  static EventHub eventHubMock;
  static FlowTx flowTx;
  Field eventHubField;
  Field objectMapperField;

  FlowTxServiceTest(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @BeforeAll
  void init() throws NoSuchFieldException, IllegalAccessException {
    Field eventHubField = FlowTxService.class.getDeclaredField("eventHub");
    eventHubField.setAccessible(true);

    Field eHubNameField = FlowTxService.class.getDeclaredField("eHubName");
    eHubNameField.setAccessible(true);

    eHubNameField.set(flowTxServiceMock, "eventHub");

    Field logField = FlowTxService.class.getDeclaredField("log");
    logField.setAccessible(true);

    objectMapperField = FlowTxService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);

    logField.set(flowTxServiceMock, Logger.getLogger(FlowTxService.class));
  }

  @BeforeEach
  void setReInterface() throws IllegalAccessException {
    //    producerField.set(reportedIuvServiceMock, producerMock);
    objectMapperField.set(flowTxServiceMock, objectMapper);
    flowTx =
        FlowTx.builder()
            // .idFlusso() //FIXME ID_FLUSSO della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .dataOraFlusso() //FIXME DATA_ORA_FLUSSO della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .insertedTimestamp() //FIXME INSERTED_TIMESTAMP della tabella
            // NODO_OFFLINE.RENDICONTAZIONE
            .dataRegolamento(Instant.now())
            .identificativoUnivocoRegolamento("")
            // .numeroTotalePagamenti() //FIXME i computed o quelli ricevuti da PSP?
            // .importoTotalePagamenti() //FIXME i computed o quelli ricevuti da PSP?
            // .idDominio() //FIXME ID_DOMINIO della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .psp() //FIXME PSP della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .intPsp() //FIXME INT_PSP della tabella NODO_OFFLINE.RENDICONTAZIONE
            .uniqueId(String.format("%s%s%s", "", "", ""))
            .dataEsitoSingoloPagamentoList(null)
            .build();
    Mockito.clearInvocations(flowTxServiceMock);
    //    Mockito.clearInvocations(producerMock);

    Mockito.doNothing().when(flowTxServiceMock).init();
    Mockito.doCallRealMethod().when(flowTxServiceMock).sendEvent(Mockito.any());
    //    Mockito.doCallRealMethod().when(reportedIuvServiceMock).writeBlobIfExist(Mockito.any());
  }

  @Test
  void testSend() {
    flowTxServiceMock.sendEvent(flowTx);
    //    Mockito.verify(reportedIuvServiceMock, Mockito.times(1)).writeBlobIfExist(Mockito.any());
  }

  //  @Test
  //  void testSend_ActionInfo() {
  //    reportedIuvServiceMock.sendEvent(reportedIuv);
  //    //    Mockito.verify(reportedIuvServiceMock,
  // Mockito.times(0)).writeBlobIfExist(Mockito.any());
  //  }

  //  @Test
  //  void testSend_Producer_Null() throws IllegalAccessException {
  //    //    producerField.set(reportedIuvServiceMock, null);
  //    reportedIuvServiceMock.sendEvent(reportedIuv);
  //    //    Mockito.verify(reportedIuvServiceMock,
  // Mockito.times(0)).writeBlobIfExist(Mockito.any());
  //  }

  //  @Test
  //  void testSend_BlobContainerClient_Null() throws IllegalAccessException {
  //    blobContainerClientField.set(reportedIuvServiceMock, null);
  //    reportedIuvServiceMock.sendEvent(reInterface);
  ////    Mockito.verify(reportedIuvServiceMock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
  //  }

  //  @Test
  //  void testSendJsonProcessingException() throws JsonProcessingException, IllegalAccessException
  // {
  //    ObjectMapper objectMapperMock = Mockito.mock(ObjectMapper.class);
  //    objectMapperField.set(reportedIuvServiceMock, objectMapperMock);
  //    Mockito.when(objectMapperMock.writeValueAsString(Mockito.any()))
  //        .thenThrow(JsonProcessingException.class);
  //
  ////    Mockito.doNothing().when(reportedIuvServiceMock).writeBlobIfExist(Mockito.any());
  //    Assert.assertThrows(AppException.class, () ->
  // reportedIuvServiceMock.sendEvent(reInterface));
  //  }
  //
  //  @Test
  //  void testSendAllEventLT0() {
  //    reportedIuvServiceMock.sendEvent((ReAbstract) null);
  //  }
  //
  //  @Test
  //  void testWriteBlobIfExist_NoReInstance() {
  ////    reportedIuvServiceMock.writeBlobIfExist(ReInternal.builder().build());
  //    Mockito.verify(Mockito.spy(blobContainerClient),
  // Mockito.times(0)).getBlobClient(Mockito.any());
  //  }
  //
  //  @Test
  //  void testWriteBlobIfExist_BodyStringBlank() {
  //    reInterface.setPayload("");
  ////    reportedIuvServiceMock.writeBlobIfExist(reInterface);
  //    Mockito.verify(Mockito.spy(blobContainerClient),
  // Mockito.times(0)).getBlobClient(Mockito.any());
  //  }

  //  @Test
  //  void testPublishEvent() throws JsonProcessingException {
  //    EventDataBatch eventDataBatch = Mockito.mock(EventDataBatch.class);
  //    Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatch);
  //    Mockito.when(eventDataBatch.tryAdd(Mockito.any())).thenReturn(true);
  //    Mockito.when(eventDataBatch.getCount()).thenReturn(1);
  //    List<EventData> eventDataList = new ArrayList<>();
  //    Mockito.doNothing().when(producerMock).send((EventDataBatch) Mockito.any());
  //    EventData eventData = new EventData(objectMapper.writeValueAsString(reInterface));
  //    eventDataList.add(eventData);
  //    Mockito.verify(producerMock, Mockito.times(1)).send((EventDataBatch) Mockito.any());
  //  }

  //  @Test
  //  void testPublishEvent_FullBatch_OK() throws JsonProcessingException {
  //    AtomicInteger counter = new AtomicInteger();
  //    EventDataBatch eventDataBatch = Mockito.mock(EventDataBatch.class);
  //    Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatch);
  //    Mockito.when(eventDataBatch.tryAdd(Mockito.any()))
  //        .thenAnswer(
  //            invocation -> {
  //              if (counter.get() == 1) return true;
  //              counter.set(1);
  //              return false;
  //            });
  //    Mockito.when(eventDataBatch.getCount()).thenReturn(1);
  //    List<EventData> eventDataList = new ArrayList<>();
  //    Mockito.doNothing().when(producerMock).send((EventDataBatch) Mockito.any());
  //    EventData eventData = new EventData(objectMapper.writeValueAsString(reInterface));
  //    eventDataList.add(eventData);
  //    Mockito.verify(producerMock, Mockito.times(2)).send((EventDataBatch) Mockito.any());
  //  }
  //
  //  @Test
  //  void testPublishEvent_TooLarge() throws JsonProcessingException {
  //    EventDataBatch eventDataBatch = Mockito.mock(EventDataBatch.class);
  //    Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatch);
  //    Mockito.when(eventDataBatch.tryAdd(Mockito.any())).thenReturn(false);
  //    Mockito.when(eventDataBatch.getCount()).thenReturn(1);
  //    List<EventData> eventDataList = new ArrayList<>();
  //    Mockito.doNothing().when(producerMock).send((EventDataBatch) Mockito.any());
  //    EventData eventData = new EventData(objectMapper.writeValueAsString(reInterface));
  //    eventDataList.add(eventData);
  //    try {
  //      fail();
  //    } catch (AppException e) {
  //      Assertions.assertEquals(AppErrorCodeMessageEnum.EVENT_HUB_TOO_LARGE, e.getCodeMessage());
  //    }
  //  }
  //
  //  @Test
  //  void testSendEventDataBachLT0() {
  //    Mockito.verify(producerMock, Mockito.times(0)).send((EventDataBatch) Mockito.any());
  //  }
}
