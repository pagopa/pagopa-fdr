package it.gov.pagopa.fdr.service.re;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.gov.pagopa.fdr.service.re.model.*;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Instant;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(AzuriteResource.class)
public class ReServiceTest {

    @Inject
    org.jboss.logging.Logger log;

    @Inject
    ObjectMapper objectMapper;

    @InjectMock
    ReService mock;

    @ConfigProperty(name = "queue.conversion.connect-str")
    String connString;

    @ConfigProperty(name = "%dev.blob.re.containername")
    static String blobName;
    BlobContainerClient blobContainerClientMock;
    static BlobServiceClient blobServiceClient;
    EventHubProducerClient producerMock;
    Field field2;
    @BeforeEach
    void init() throws NoSuchFieldException, IllegalAccessException {

        Field field1 = ReService.class.getDeclaredField("producer");
        field2 = ReService.class.getDeclaredField("blobContainerClient");
        Field field3 = ReService.class.getDeclaredField("blobContainerName");
        Field field4 = ReService.class.getDeclaredField("eHubName");

        blobServiceClient = new BlobServiceClientBuilder().connectionString(connString).buildClient();
        blobContainerClientMock = blobServiceClient.createBlobContainerIfNotExists(blobName);
        producerMock = Mockito.mock(EventHubProducerClient.class);

        field1.setAccessible(true);
        field2.setAccessible(true);

        field1.set(mock, producerMock);
        field2.set(mock, blobContainerClientMock);
        field3.set(mock, "blobcontainerre");
        field4.set(mock, "eventHub");

        Field logField = ReService.class.getDeclaredField("log");
        Field objectMapperField = ReService.class.getDeclaredField("objectMapper");
        logField.set(mock, Logger.getLogger(ReService.class));
        objectMapperField.set(mock, objectMapper);

        Mockito.doNothing().when(mock).init();
        Mockito.doNothing().when(mock).publishEvents(Mockito.any());
        Mockito.doCallRealMethod().when(mock).sendEvent(Mockito.any(ReInterface.class));
        Mockito.doCallRealMethod().when(mock).writeBlobIfExist(Mockito.any());
    }

    @Test
    public void testSend() {
        ReInterface reInterface =
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
                        .build()
        ;

        mock.sendEvent(reInterface);
        Mockito.verify(mock, Mockito.times(1)).sendEvent(reInterface);
        Mockito.verify(mock, Mockito.times(1)).writeBlobIfExist(Mockito.any());
    }
    @Test
    public void testSendActionInfo() {
        ReInterface reInterface =
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
                .fdrAction(FdrActionEnum.INFO)
                .build()
            ;

        mock.sendEvent(reInterface);
        Mockito.verify(mock, Mockito.times(1)).sendEvent(reInterface);
        Mockito.verify(mock, Mockito.times(0)).writeBlobIfExist(Mockito.any());
        Mockito.verify(mock, Mockito.times(0)).publishEvents(Mockito.any());
    }
}