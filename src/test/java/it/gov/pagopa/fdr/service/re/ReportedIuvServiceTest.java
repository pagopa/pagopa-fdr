package it.gov.pagopa.fdr.service.re;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.gov.pagopa.fdr.service.re.model.*;
import it.gov.pagopa.fdr.service.reportedIuv.ReportedIuvService;
import it.gov.pagopa.fdr.service.reportedIuv.model.ReportedIuv;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
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
    Field eventHubField = ReportedIuvService.class.getDeclaredField("eventHub");
    eventHubField.setAccessible(true);

    Field eHubNameField = ReportedIuvService.class.getDeclaredField("eHubName");
    eHubNameField.setAccessible(true);

    eHubNameField.set(reportedIuvServiceMock, "eventHub");

    Field logField = ReportedIuvService.class.getDeclaredField("log");
    logField.setAccessible(true);

    objectMapperField = ReportedIuvService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);

    logField.set(reportedIuvServiceMock, Logger.getLogger(ReportedIuvService.class));
  }

  @BeforeEach
  void setReInterface() throws IllegalAccessException {
    //    producerField.set(reportedIuvServiceMock, producerMock);
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
    //    Mockito.clearInvocations(producerMock);

    Mockito.doNothing().when(reportedIuvServiceMock).init();
    Mockito.doCallRealMethod().when(reportedIuvServiceMock).sendEvent(Mockito.anyList());
    //    Mockito.doCallRealMethod().when(reportedIuvServiceMock).writeBlobIfExist(Mockito.any());
  }

  @Test
  void testSend() {
    reportedIuvServiceMock.sendEvent(reportedIuv);
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
