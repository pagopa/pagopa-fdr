package it.gov.pagopa.fdr.storage;

import static it.gov.pagopa.fdr.test.util.TestUtil.validFlowBlob;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.repository.enums.BlobProcessingStatusEnum;
import it.gov.pagopa.fdr.repository.enums.BlobTagNameEnum;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import it.gov.pagopa.fdr.util.constant.AppConstant;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@QuarkusTest
class HistoryBlobStorageServiceTest {

  @InjectMock BlobContainerAsyncClient blobContainerAsyncClient;

  @Inject HistoryBlobStorageService historyBlobStorageService;

  @Test
  void saveJsonFileOk_external() throws IOException {

    BlobAsyncClient blobClient = Mockito.mock(BlobAsyncClient.class);
    FlowToHistoryEntity flowToHistoryEntity =
        Mockito.mock(FlowToHistoryEntity.class);

    when(blobContainerAsyncClient.getBlobAsyncClient(anyString()))
        .thenReturn(blobClient);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(true);

    when(blobClient.uploadWithResponse(any(BlobParallelUploadOptions.class)))
        .thenReturn(Mono.empty());

    when(blobClient.getTags())
        .thenReturn(Mono.just(new HashMap<>()));

    Instant before = Instant.now();

    historyBlobStorageService.saveJsonFile(
        validFlowBlob(),
        flowToHistoryEntity
    );

    Instant after = Instant.now();

    ArgumentCaptor<BlobParallelUploadOptions> captor =
        ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

    verify(blobClient).uploadWithResponse(captor.capture());

    BlobParallelUploadOptions options = captor.getValue();

    Map<String, String> metadata = options.getMetadata();

    assertNotNull(metadata);
    assertEquals("true", metadata.get("elaborate"));
    assertNotNull(metadata.get("sessionId"));
    assertNotNull(metadata.get("insertedTimestamp"));
    assertEquals(
        AppConstant.SERVICE_IDENTIFIER,
        metadata.get("serviceIdentifier")
    );

    assertDoesNotThrow(() ->
        UUID.fromString(metadata.get("sessionId"))
    );

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

    String updatedAt =
        tags.get(BlobTagNameEnum.PROCESSING_UPDATED_AT.getValue());

    assertNotNull(updatedAt);

    Instant timestamp = Instant.parse(updatedAt);

    assertEquals(
        timestamp.truncatedTo(ChronoUnit.SECONDS),
        timestamp
    );

    assertFalse(
        timestamp.isBefore(before.truncatedTo(ChronoUnit.SECONDS))
    );

    assertFalse(timestamp.isAfter(after));

    assertNull(options.getRequestConditions());
  }

  @Test
  void saveJsonFileOk_internal() throws IOException {

    BlobAsyncClient blobClient = Mockito.mock(BlobAsyncClient.class);
    FlowToHistoryEntity flowToHistoryEntity =
        Mockito.mock(FlowToHistoryEntity.class);

    when(blobContainerAsyncClient.getBlobAsyncClient(anyString()))
        .thenReturn(blobClient);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(false);

    when(blobClient.uploadWithResponse(any(BlobParallelUploadOptions.class)))
        .thenReturn(Mono.empty());

    when(blobClient.getTags())
        .thenReturn(Mono.just(new HashMap<>()));

    historyBlobStorageService.saveJsonFile(
        validFlowBlob(),
        flowToHistoryEntity
    );

    ArgumentCaptor<BlobParallelUploadOptions> captor =
        ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

    verify(blobClient).uploadWithResponse(captor.capture());

    BlobParallelUploadOptions options = captor.getValue();

    Map<String, String> metadata = options.getMetadata();

    assertEquals("false", metadata.get("elaborate"));

    Map<String, String> tags = options.getTags();

    assertEquals(3, tags.size());

    assertEquals(
        BlobProcessingStatusEnum.DA_ELABORARE.name(),
        tags.get(BlobTagNameEnum.PROCESSING_STATUS.getValue())
    );

    assertEquals(
        "0",
        tags.get(BlobTagNameEnum.PROCESSING_ATTEMPT.getValue())
    );

    assertDoesNotThrow(() ->
        Instant.parse(
            tags.get(BlobTagNameEnum.PROCESSING_UPDATED_AT.getValue())
        )
    );

    assertNull(options.getRequestConditions());

    verify(blobClient, never()).setMetadata(anyMap());
    verify(blobClient, never()).setTags(anyMap());
  }

  @Test
  void saveJsonFileKo() {
    BlobAsyncClient blobClient = Mockito.mock(BlobAsyncClient.class);
    FlowToHistoryEntity flowToHistoryEntity = Mockito.mock(FlowToHistoryEntity.class);
    when(blobContainerAsyncClient.getBlobAsyncClient(anyString())).thenReturn(blobClient);
    when(flowToHistoryEntity.getIsExternal()).thenReturn(true);
    FlowBlob build = FlowBlob.builder().build();
    assertThrows(
        Exception.class,
        () -> {
          historyBlobStorageService.saveJsonFile(build, flowToHistoryEntity);
        });
  }

  @Test
  void saveJsonFile_uploadFailureShouldBeLogged() throws IOException {

    BlobAsyncClient blobClient = Mockito.mock(BlobAsyncClient.class);
    FlowToHistoryEntity flowToHistoryEntity =
        Mockito.mock(FlowToHistoryEntity.class);

    when(blobContainerAsyncClient.getBlobAsyncClient(anyString()))
        .thenReturn(blobClient);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(true);

    RuntimeException uploadError =
        new RuntimeException("Azure Blob upload failed");

    when(blobClient.uploadWithResponse(any(BlobParallelUploadOptions.class)))
        .thenReturn(Mono.error(uploadError));

    assertDoesNotThrow(() ->
        historyBlobStorageService.saveJsonFile(
            validFlowBlob(),
            flowToHistoryEntity
        )
    );

    verify(blobClient).uploadWithResponse(
        any(BlobParallelUploadOptions.class)
    );

    verify(blobClient, never()).getTags();
  }

  @Test
  void saveJsonFileKo_invalidJsonSchema() {

    FlowToHistoryEntity flowToHistoryEntity =
        Mockito.mock(FlowToHistoryEntity.class);

    FlowBlob invalidFlowBlob = validFlowBlob();

    invalidFlowBlob.setFdr(null);

    assertThrows(
        ValidationException.class,
        () -> historyBlobStorageService.saveJsonFile(
            invalidFlowBlob,
            flowToHistoryEntity
        )
    );
  }

  @Test
  void saveJsonFile_shouldLogSavedTags() throws IOException {

    BlobAsyncClient blobClient = Mockito.mock(BlobAsyncClient.class);
    FlowToHistoryEntity flowToHistoryEntity =
        Mockito.mock(FlowToHistoryEntity.class);

    when(blobContainerAsyncClient.getBlobAsyncClient(anyString()))
        .thenReturn(blobClient);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(true);

    when(blobClient.uploadWithResponse(any(BlobParallelUploadOptions.class)))
        .thenReturn(Mono.just(
            new SimpleResponse<>(
                null,
                201,
                new HttpHeaders(),
                Mockito.mock(BlockBlobItem.class)
            )
        ));

    Map<String, String> tags = Map.of(
        "processingStatus", "DA_ELABORARE",
        "processingAttempt", "0",
        "processingUpdatedAt", "2026-11-12T09:34:49Z"
    );

    when(blobClient.getTags())
        .thenReturn(Mono.just(tags));

    historyBlobStorageService.saveJsonFile(
        validFlowBlob(),
        flowToHistoryEntity
    );

    verify(blobClient).uploadWithResponse(
        any(BlobParallelUploadOptions.class)
    );

    verify(blobClient).getTags();
  }
}
