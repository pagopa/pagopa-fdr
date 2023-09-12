package it.gov.pagopa.fdr.service.re;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.re.model.*;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bson.assertions.Assertions.fail;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(AzuriteResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReServiceTest {
    @Inject
    ObjectMapper objectMapper;
    @InjectMock
    ReService reServiceMock;
    @ConfigProperty(name = "blob.re.connect-str")
    String blobConnString;
    @ConfigProperty(name = "%dev.blob.re.containername")
    static String blobName;
    BlobContainerClient blobContainerClient;
    static BlobServiceClient blobServiceClient;
    static EventHubProducerClient producerMock;
    static ReInterface reInterface;
    Field producerField;
    Field blobContainerClientField;
    Field objectMapperField;

    @BeforeAll
    void init() throws NoSuchFieldException, IllegalAccessException {
        producerField = ReService.class.getDeclaredField("producer");
        blobContainerClientField = ReService.class.getDeclaredField("blobContainerClient");
        Field blobContainerNameField = ReService.class.getDeclaredField("blobContainerName");
        Field eHubNameField = ReService.class.getDeclaredField("eHubName");
        blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnString).buildClient();
        blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(blobName);
        producerMock = Mockito.mock(EventHubProducerClient.class);

        producerField.setAccessible(true);
        blobContainerClientField.setAccessible(true);

        blobContainerClientField.set(reServiceMock, blobContainerClient);
        blobContainerNameField.set(reServiceMock, "blobcontainerre");
        eHubNameField.set(reServiceMock, "eventHub");

        Field logField = ReService.class.getDeclaredField("log");
        objectMapperField = ReService.class.getDeclaredField("objectMapper");
        logField.set(reServiceMock, Logger.getLogger(ReService.class));
    }
    @BeforeEach
    public void setReInterface() throws IllegalAccessException {
        producerField.set(reServiceMock, producerMock);
        blobContainerClientField.set(reServiceMock, blobContainerClient);
        objectMapperField.set(reServiceMock,objectMapper);
        reInterface =
                ReInterface.builder()
                        .uniqueId("123")
                        .appVersion(AppVersionEnum.FDR003)
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
    public void testSend() {
        reServiceMock.sendEvent(reInterface);
        Mockito.verify(reServiceMock, Mockito.times(1)).writeBlobIfExist(Mockito.any());
        Mockito.verify(reServiceMock, Mockito.times(1)).publishEvents(Mockito.any());
    }
    @Test
    public void testSend_ActionInfo() {
        reInterface.setFdrAction(FdrActionEnum.INFO);
        reServiceMock.sendEvent(reInterface);
        Mockito.verify(reServiceMock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
        Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(Mockito.any());
    }

    @Test
    public void testSend_Producer_Null() throws IllegalAccessException {
        producerField.set(reServiceMock, null);
        reServiceMock.sendEvent(reInterface);
        Mockito.verify(reServiceMock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
        Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(Mockito.any());
    }

    @Test
    public void testSend_BlobContainerClient_Null() throws IllegalAccessException {
        blobContainerClientField.set(reServiceMock, null);
        reServiceMock.sendEvent(reInterface);
        Mockito.verify(reServiceMock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
        Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(Mockito.any());
    }

    @Test
    public void testSendJsonProcessingException() throws JsonProcessingException, IllegalAccessException {
        ObjectMapper objectMapperMock = Mockito.mock(ObjectMapper.class);
        objectMapperField.set(reServiceMock,objectMapperMock);
        Mockito.when(objectMapperMock.writeValueAsString(Mockito.any())).thenThrow(JsonProcessingException.class);

        Mockito.doNothing().when(reServiceMock).writeBlobIfExist(Mockito.any());
        Assert.assertThrows(AppException.class, () -> {
            reServiceMock.sendEvent(reInterface);
        });
    }

    @Test
    public void testSendAllEventGT0(){
        reServiceMock.sendEvent(null);
        Mockito.verify(reServiceMock, Mockito.times(0)).publishEvents(null);
    }
    @Test
    public void testWriteBlobIfExist_NoReInstance() {
        reServiceMock.writeBlobIfExist(ReInternal.builder().build());
        Mockito.verify(Mockito.spy(blobContainerClient), Mockito.times(0)).getBlobClient(Mockito.any());
    }

    @Test
    public void testWriteBlobIfExist_BodyStringBlank() {
        reInterface.setPayload("");
        reServiceMock.writeBlobIfExist(reInterface);
        Mockito.verify(Mockito.spy(blobContainerClient), Mockito.times(0)).getBlobClient(Mockito.any());
    }

    @Test
    public void testPublishEvent() throws JsonProcessingException {
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
        Mockito.verify(producerMock,Mockito.times(1)).send((EventDataBatch) Mockito.any());
    }

    @Test
    public void testPublishEvent_FullBatch_OK() throws JsonProcessingException {
        AtomicInteger counter = new AtomicInteger();
        Mockito.doCallRealMethod().when(reServiceMock).publishEvents(Mockito.any());
        EventDataBatch eventDataBatch = Mockito.mock(EventDataBatch.class);
        Mockito.when(producerMock.createBatch()).thenReturn(eventDataBatch);
        Mockito.when(eventDataBatch.tryAdd(Mockito.any())).thenAnswer(
                invocation -> {
                    if(counter.get() == 1){
                        return true;
                    }else{
                        counter.set(1);
                        return false;
                    }
                });
        Mockito.when(eventDataBatch.getCount()).thenReturn(1);
        List<EventData> eventDataList = new ArrayList<>();
        Mockito.doNothing().when(producerMock).send((EventDataBatch) Mockito.any());
        EventData eventData = new EventData(objectMapper.writeValueAsString(reInterface));
        eventDataList.add(eventData);
        reServiceMock.publishEvents(eventDataList);
        Mockito.verify(producerMock,Mockito.times(2)).send((EventDataBatch) Mockito.any());
    }

    @Test
    public void testPublishEvent_TooLarge() throws JsonProcessingException {
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
        }catch(AppException e){
            Assert.assertEquals(AppErrorCodeMessageEnum.EVENT_HUB_RE_TOO_LARGE, e.getCodeMessage());
        }
    }

    @Test
    public void testSendEventDataBachGT0(){
        reServiceMock.publishEvents(null);
        Mockito.verify(producerMock, Mockito.times(0)).send((EventDataBatch) Mockito.any());
    }
}