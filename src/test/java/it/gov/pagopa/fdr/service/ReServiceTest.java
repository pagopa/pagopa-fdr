package it.gov.pagopa.fdr.service;

import com.azure.core.util.Context;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.entity.re.ReEventEntity;
import it.gov.pagopa.fdr.repository.enums.BlobProcessingStatusEnum;
import it.gov.pagopa.fdr.repository.enums.BlobTagNameEnum;
import it.gov.pagopa.fdr.service.middleware.mapper.ReEventMapper;
import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import it.gov.pagopa.fdr.service.model.re.ReEvent;
import it.gov.pagopa.fdr.test.util.AzuriteResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import java.lang.reflect.Field;
import java.time.Instant;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.mockito.ArgumentCaptor;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(AzuriteResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReServiceTest {

  /*
  private final ObjectMapper objectMapper;
  @InjectMock ReService reServiceMock;

  @ConfigProperty(name = "blob.re.connect-str")
  String blobConnString;

  @ConfigProperty(name = "%dev.blob.re.containername")
  String blobName;

  BlobContainerClient blobContainerClient;
  static BlobServiceClient blobServiceClient;
  static EventHubProducerClient producerMock;
  static ReInterface reInterface;
  Field producerField;
  Field blobContainerClientField;
  Field objectMapperField;

  ReServiceTest(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @BeforeAll
  void init() throws NoSuchFieldException, IllegalAccessException {
    producerField = ReService.class.getDeclaredField("producer");
    blobContainerClientField = ReService.class.getDeclaredField("blobContainerClient");
    Field blobContainerNameField = ReService.class.getDeclaredField("blobContainerName");
    Field eHubNameField = ReService.class.getDeclaredField("eHubName");
    blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobConnString).buildClient();
    blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(blobName);
    producerMock = Mockito.mock(EventHubProducerClient.class);

    producerField.setAccessible(true);
    blobContainerClientField.setAccessible(true);

    blobContainerClientField.set(reServiceMock, blobContainerClient);
    blobContainerNameField.set(reServiceMock, blobName);
    eHubNameField.set(reServiceMock, "eventHub");

    Field logField = ReService.class.getDeclaredField("log");
    logField.setAccessible(true);
    objectMapperField = ReService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    logField.set(reServiceMock, Logger.getLogger(ReService.class));
  }

  @BeforeEach
  void setReInterface() throws IllegalAccessException {
    producerField.set(reServiceMock, producerMock);
    blobContainerClientField.set(reServiceMock, blobContainerClient);
    objectMapperField.set(reServiceMock, objectMapper);
    reInterface =
        ReInterface.builder()
            .uniqueId("123")
            .serviceIdentifier(AppVersionEnum.FDR003)
            .created(Instant.now())
            .sessionId("sessionId")
            .eventType(EventTypeEnum.INTERFACE)
            .httpType(HttpTypeEnum.RES)
            .httpMethod("GET")
            .httpUrl("requestPath")
            .payload("responsePayload")
            .pspId("1")
            .fdr("1")
            .organizationId("1")
            .fdrAction(FdrActionEnum.GET_FDR)
            .build();
    Mockito.clearInvocations(reServiceMock);
    Mockito.clearInvocations(producerMock);

    Mockito.doNothing().when(reServiceMock).init();
    Mockito.doNothing().when(reServiceMock).publishEvents(Mockito.any());
    Mockito.doCallRealMethod().when(reServiceMock).sendEvent(Mockito.any(ReInterface.class));
    Mockito.doCallRealMethod().when(reServiceMock).writeBlobIfExist(Mockito.any());
  }

  @Test
  void testSend() {
    reServiceMock.sendEvent(reInterface);
    Mockito.verify(reServiceMock, Mockito.times(1)).writeBlobIfExist(Mockito.any());
    Mockito.verify(reServiceMock, Mockito.times(1)).publishEvents(Mockito.any());
  }

  @Test
  void testSend_ActionInfo() {
    reInterface.setFdrAction(FdrActionEnum.INFO);
    reServiceMock.sendEvent(reInterface);
    Mockito.verify(reServiceMock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
    Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(Mockito.any());
  }

  @Test
  void testSend_Producer_Null() throws IllegalAccessException {
    producerField.set(reServiceMock, null);
    reServiceMock.sendEvent(reInterface);
    Mockito.verify(reServiceMock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
    Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(Mockito.any());
  }

  @Test
  void testSend_BlobContainerClient_Null() throws IllegalAccessException {
    blobContainerClientField.set(reServiceMock, null);
    reServiceMock.sendEvent(reInterface);
    Mockito.verify(reServiceMock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
    Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(Mockito.any());
  }

  @Test
  void testSendJsonProcessingException() throws JsonProcessingException, IllegalAccessException {
    ObjectMapper objectMapperMock = Mockito.mock(ObjectMapper.class);
    objectMapperField.set(reServiceMock, objectMapperMock);
    Mockito.when(objectMapperMock.writeValueAsString(Mockito.any()))
        .thenThrow(JsonProcessingException.class);

    Mockito.doNothing().when(reServiceMock).writeBlobIfExist(Mockito.any());
    Assert.assertThrows(AppException.class, () -> reServiceMock.sendEvent(reInterface));
  }

  @Test
  void testSendAllEventLT0() {
    reServiceMock.sendEvent((ReAbstract) null);
    Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(null);
  }

  @Test
  void testWriteBlobIfExist_NoReInstance() {
    reServiceMock.writeBlobIfExist(ReInternal.builder().build());
    Mockito.verify(Mockito.spy(blobContainerClient), Mockito.times(0)).getBlobClient(Mockito.any());
  }

  @Test
  void testWriteBlobIfExist_BodyStringBlank() {
    reInterface.setPayload("");
    reServiceMock.writeBlobIfExist(reInterface);
    Mockito.verify(Mockito.spy(blobContainerClient), Mockito.times(0)).getBlobClient(Mockito.any());
  }

  @Test
  void testPublishEvent() throws JsonProcessingException {
    Mockito.doCallRealMethod().when(reServiceMock).publishEvents(Mockito.any());
    EventDataBatch eventDataBatch = Mockito.mock(EventDataBatch.class);
    Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatch);
    Mockito.when(eventDataBatch.tryAdd(Mockito.any())).thenReturn(true);
    Mockito.when(eventDataBatch.getCount()).thenReturn(1);
    List<EventData> eventDataList = new ArrayList<>();
    Mockito.doNothing().when(producerMock).send((EventDataBatch) Mockito.any());
    EventData eventData = new EventData(objectMapper.writeValueAsString(reInterface));
    eventDataList.add(eventData);
    reServiceMock.publishEvents(eventDataList);
    Mockito.verify(producerMock, Mockito.times(1)).send((EventDataBatch) Mockito.any());
  }

  @Test
  void testPublishEvent_FullBatch_OK() throws JsonProcessingException {
    AtomicInteger counter = new AtomicInteger();
    Mockito.doCallRealMethod().when(reServiceMock).publishEvents(Mockito.any());
    EventDataBatch eventDataBatch = Mockito.mock(EventDataBatch.class);
    Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatch);
    Mockito.when(eventDataBatch.tryAdd(Mockito.any()))
        .thenAnswer(
            invocation -> {
              if (counter.get() == 1) {
                return true;
              }
              counter.set(1);
              return false;
            });
    Mockito.when(eventDataBatch.getCount()).thenReturn(1);
    List<EventData> eventDataList = new ArrayList<>();
    Mockito.doNothing().when(producerMock).send((EventDataBatch) Mockito.any());
    EventData eventData = new EventData(objectMapper.writeValueAsString(reInterface));
    eventDataList.add(eventData);
    reServiceMock.publishEvents(eventDataList);
    Mockito.verify(producerMock, Mockito.times(2)).send((EventDataBatch) Mockito.any());
  }

  @Test
  void testPublishEvent_TooLarge() throws JsonProcessingException {
    Mockito.doCallRealMethod().when(reServiceMock).publishEvents(Mockito.any());
    EventDataBatch eventDataBatch = Mockito.mock(EventDataBatch.class);
    Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatch);
    Mockito.when(eventDataBatch.tryAdd(Mockito.any())).thenReturn(false);
    Mockito.when(eventDataBatch.getCount()).thenReturn(1);
    List<EventData> eventDataList = new ArrayList<>();
    Mockito.doNothing().when(producerMock).send((EventDataBatch) Mockito.any());
    EventData eventData = new EventData(objectMapper.writeValueAsString(reInterface));
    eventDataList.add(eventData);
    try {
      reServiceMock.publishEvents(eventDataList);
      fail();
    } catch (AppException e) {
      Assertions.assertEquals(AppErrorCodeMessageEnum.EVENT_HUB_RE_TOO_LARGE, e.getCodeMessage());
    }
  }

  @Test
  void testSendEventDataBachLT0() {
    reServiceMock.publishEvents(null);
    Mockito.verify(producerMock, Mockito.times(0)).send((EventDataBatch) Mockito.any());
  }*/
    
    @Test
    void storeEvent_shouldBuildBlobPathUsingEuropeRomeTimezone() throws Exception {
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        ReEventMapper reEventMapper = mock(ReEventMapper.class);
        ReEventEntity entity = mock(ReEventEntity.class);
        Logger log = mock(Logger.class);

        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobContainerClient.getAccountName()).thenReturn("storage-account");
        when(reEventMapper.toEntity(any(ReEvent.class))).thenReturn(entity);
        doNothing().when(entity).persist();

        ReService service = new ReService(log, reEventMapper, blobContainerClient, 1);
        setField(service, "blobContainerName", "re-container");

        ReEvent reEvent =
                ReEvent.builder()
                .sessionId("SESSION1")
                .created(Instant.parse("2026-03-24T23:30:00Z"))
                .fdrAction(FdrActionEnum.CREATE_FLOW)
                .reqPayload("{\"ok\":true}")
                .build();

        service.storeEvent(reEvent);

        assertNotNull(reEvent.getReqBodyRef());
        assertNotNull(reEvent.getReqBodyRef().getFileName());
        assertTrue(reEvent.getReqBodyRef().getFileName().startsWith("2026/03/25/00/"));
        assertTrue(reEvent.getReqBodyRef().getFileName().contains("SESSION1_CREATE_FLOW_REQ.json.zip"));
    }

    @Test
    void storeEvent_shouldKeepSameDateWhenUtcAndEuropeRomeAreSameDay() throws Exception {
        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        ReEventMapper reEventMapper = mock(ReEventMapper.class);
        ReEventEntity entity = mock(ReEventEntity.class);
        Logger log = mock(Logger.class);

        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobContainerClient.getAccountName()).thenReturn("storage-account");
        when(reEventMapper.toEntity(any(ReEvent.class))).thenReturn(entity);
        doNothing().when(entity).persist();

        ReService service = new ReService(log, reEventMapper, blobContainerClient, 1);
        setField(service, "blobContainerName", "re-container");

        ReEvent reEvent =
                ReEvent.builder()
                .sessionId("SESSION1")
                .created(Instant.parse("2026-03-24T10:15:00Z"))
                .fdrAction(FdrActionEnum.CREATE_FLOW)
                .reqPayload("{\"ok\":true}")
                .build();

        service.storeEvent(reEvent);

        assertNotNull(reEvent.getReqBodyRef());
        assertNotNull(reEvent.getReqBodyRef().getFileName());
        assertTrue(reEvent.getReqBodyRef().getFileName().startsWith("2026/03/24/11/"));
        assertTrue(reEvent.getReqBodyRef().getFileName().contains("SESSION1_CREATE_FLOW_REQ.json.zip"));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }



    @Test
    void storeEvent_shouldInitializeProcessingTags() throws Exception {

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        ReEventMapper reEventMapper = mock(ReEventMapper.class);
        ReEventEntity entity = mock(ReEventEntity.class);
        Logger log = mock(Logger.class);

        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobContainerClient.getAccountName()).thenReturn("storage-account");
        when(reEventMapper.toEntity(any(ReEvent.class))).thenReturn(entity);

        ReService service = new ReService(
            log,
            reEventMapper,
            blobContainerClient,
            1
        );

        setField(service, "blobContainerName", "re-container");

        ReEvent reEvent = ReEvent.builder()
            .sessionId("SESSION1")
            .created(Instant.parse("2026-11-11T10:00:00Z"))
            .fdrAction(FdrActionEnum.CREATE_FLOW)
            .reqPayload("{\"test\":true}")
            .build();

        Instant before = Instant.now();

        service.storeEvent(reEvent);

        Instant after = Instant.now();

        ArgumentCaptor<BlobParallelUploadOptions> captor =
            ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

        verify(blobClient).uploadWithResponse(
            captor.capture(),
            isNull(),
            eq(Context.NONE)
        );

        BlobParallelUploadOptions options = captor.getValue();

        Map<String, String> tags = options.getTags();

        assertNotNull(tags);
        assertEquals(3, tags.size());

        assertEquals(
            BlobProcessingStatusEnum.DA_ELABORARE.name(),
            tags.get(BlobTagNameEnum.PROCESSING_STATUS.getValue())
        );

        assertEquals(
            "0",
            tags.get(BlobTagNameEnum.PROCESSING_ATTEMPT.getValue())
        );

        String updatedAt = tags.get(
            BlobTagNameEnum.PROCESSING_UPDATED_AT.getValue()
        );

        assertNotNull(updatedAt);

        Instant timestamp = Instant.parse(updatedAt);

        assertEquals(
            timestamp.truncatedTo(ChronoUnit.SECONDS),
            timestamp
        );

        assertFalse(
            timestamp.isBefore(
                before.truncatedTo(ChronoUnit.SECONDS)
            )
        );

        assertFalse(timestamp.isAfter(after));

        assertEquals(
            "*",
            options.getRequestConditions().getIfNoneMatch()
        );

        verify(blobClient, never()).setTags(anyMap());

        assertNotNull(reEvent.getReqBodyRef());

        service.shutdown();
    }


    @Test
    void storeEvent_shouldInitializeTagsForRequestAndResponse() throws Exception {

        BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
        BlobClient reqBlobClient = mock(BlobClient.class);
        BlobClient resBlobClient = mock(BlobClient.class);
        ReEventMapper reEventMapper = mock(ReEventMapper.class);
        ReEventEntity entity = mock(ReEventEntity.class);
        Logger log = mock(Logger.class);

        when(blobContainerClient.getBlobClient(anyString()))
            .thenAnswer(invocation -> {
                String fileName = invocation.getArgument(0);

                return fileName.contains("_REQ.json.zip")
                    ? reqBlobClient
                    : resBlobClient;
            });

        when(blobContainerClient.getAccountName()).thenReturn("storage-account");
        when(reEventMapper.toEntity(any(ReEvent.class))).thenReturn(entity);

        ReService service = new ReService(
            log,
            reEventMapper,
            blobContainerClient,
            1
        );

        setField(service, "blobContainerName", "re-container");

        ReEvent reEvent = ReEvent.builder()
            .sessionId("SESSION1")
            .created(Instant.parse("2026-11-11T10:00:00Z"))
            .fdrAction(FdrActionEnum.CREATE_FLOW)
            .reqPayload("{\"request\":true}")
            .resPayload("{\"response\":true}")
            .build();

        service.storeEvent(reEvent);

        ArgumentCaptor<BlobParallelUploadOptions> reqCaptor =
            ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

        ArgumentCaptor<BlobParallelUploadOptions> resCaptor =
            ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

        verify(reqBlobClient).uploadWithResponse(
            reqCaptor.capture(),
            isNull(),
            eq(Context.NONE)
        );

        verify(resBlobClient).uploadWithResponse(
            resCaptor.capture(),
            isNull(),
            eq(Context.NONE)
        );

        Map<String, String> reqTags = reqCaptor.getValue().getTags();
        Map<String, String> resTags = resCaptor.getValue().getTags();

        for (Map<String, String> tags : List.of(reqTags, resTags)) {

            assertEquals(3, tags.size());

            assertEquals(
                "DA_ELABORARE",
                tags.get(BlobTagNameEnum.PROCESSING_STATUS.getValue())
            );

            assertEquals(
                "0",
                tags.get(BlobTagNameEnum.PROCESSING_ATTEMPT.getValue())
            );

            assertNotNull(
                tags.get(BlobTagNameEnum.PROCESSING_UPDATED_AT.getValue())
            );

            assertDoesNotThrow(() ->
                Instant.parse(
                    tags.get(
                        BlobTagNameEnum.PROCESSING_UPDATED_AT.getValue()
                    )
                )
            );
        }

        assertEquals(
            "*",
            reqCaptor.getValue()
                .getRequestConditions()
                .getIfNoneMatch()
        );

        assertEquals(
            "*",
            resCaptor.getValue()
                .getRequestConditions()
                .getIfNoneMatch()
        );

        verify(reqBlobClient, never()).setTags(anyMap());
        verify(resBlobClient, never()).setTags(anyMap());

        assertNotNull(reEvent.getReqBodyRef());
        assertNotNull(reEvent.getResBodyRef());

        service.shutdown();
    }

}
