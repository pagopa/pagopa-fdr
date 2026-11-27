package it.gov.pagopa.fdr.storage;

import static it.gov.pagopa.fdr.test.util.TestUtil.validFlowBlob;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;

@QuarkusTest
class HistoryBlobStorageServiceTest {

  @InjectMock
  BlobContainerClient blobContainerClient;

  @Inject HistoryBlobStorageService historyBlobStorageService;

  @Test
  void saveJsonFileOk_external() throws IOException {

    BlobClient blobClient = mock(BlobClient.class);
    FlowToHistoryEntity flowToHistoryEntity =
        mock(FlowToHistoryEntity.class);

    when(blobContainerClient.getBlobClient(anyString()))
        .thenReturn(blobClient);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(true);

    when(blobClient.uploadWithResponse(
        any(BlobParallelUploadOptions.class),
        isNull(),
        eq(Context.NONE)))
        .thenReturn(
            new SimpleResponse<>(
                null,
                201,
                new HttpHeaders(),
                mock(BlockBlobItem.class)));

    Instant before = Instant.now();

    historyBlobStorageService.saveJsonFile(
        validFlowBlob(),
        flowToHistoryEntity
    );

    Instant after = Instant.now();

    ArgumentCaptor<BlobParallelUploadOptions> captor =
        ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

    verify(blobClient).uploadWithResponse(
        captor.capture(),
        isNull(),
        eq(Context.NONE));

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
        BlobProcessingStatusEnum.PENDING.name(),
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

    BlobClient blobClient = mock(BlobClient.class);
    FlowToHistoryEntity flowToHistoryEntity =
        mock(FlowToHistoryEntity.class);

    when(blobContainerClient.getBlobClient(anyString()))
        .thenReturn(blobClient);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(false);

    when(blobClient.uploadWithResponse(
        any(BlobParallelUploadOptions.class),
        isNull(),
        eq(Context.NONE)))
        .thenReturn(
            new SimpleResponse<>(
                null,
                201,
                new HttpHeaders(),
                mock(BlockBlobItem.class)));

    historyBlobStorageService.saveJsonFile(
        validFlowBlob(),
        flowToHistoryEntity
    );

    ArgumentCaptor<BlobParallelUploadOptions> captor =
        ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

    verify(blobClient).uploadWithResponse(
        captor.capture(),
        isNull(),
        eq(Context.NONE));

    BlobParallelUploadOptions options = captor.getValue();

    Map<String, String> metadata = options.getMetadata();

    assertNotNull(metadata);
    assertEquals("false", metadata.get("elaborate"));
    assertNotNull(metadata.get("sessionId"));
    assertNotNull(metadata.get("insertedTimestamp"));

    assertEquals(
        AppConstant.SERVICE_IDENTIFIER,
        metadata.get("serviceIdentifier")
    );

    assertNull(options.getTags());

    assertNull(options.getRequestConditions());

    verify(blobClient, never()).setMetadata(anyMap());
    verify(blobClient, never()).setTags(anyMap());
    verify(blobClient, never()).getTags();
  }

  @Test
  void saveJsonFileKo() {
    FlowToHistoryEntity flowToHistoryEntity = mock(FlowToHistoryEntity.class);
    FlowBlob build = FlowBlob.builder().build();
    assertThrows(
        Exception.class,
        () -> {
          historyBlobStorageService.saveJsonFile(build, flowToHistoryEntity);
        });
    verifyNoInteractions(blobContainerClient);
  }

  @Test
  void saveJsonFile_uploadFailureShouldBePropagated() {

    BlobClient blobClient = mock(BlobClient.class);

    FlowToHistoryEntity flowToHistoryEntity =
        mock(FlowToHistoryEntity.class);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(true);

    when(blobContainerClient.getBlobClient(anyString()))
        .thenReturn(blobClient);

    FlowBlob flowBlob = validFlowBlob();

    RuntimeException uploadError =
        new RuntimeException("Azure Blob upload failed");

    when(blobClient.uploadWithResponse(
        any(BlobParallelUploadOptions.class),
        isNull(),
        eq(Context.NONE)))
        .thenThrow(uploadError);

    RuntimeException actualException =
        assertThrows(
            RuntimeException.class,
            () -> historyBlobStorageService.saveJsonFile(
                flowBlob, flowToHistoryEntity));

    assertSame(uploadError, actualException);

    verify(blobClient).uploadWithResponse(
        any(BlobParallelUploadOptions.class),
        isNull(),
        eq(Context.NONE));

    verify(blobClient, never()).getTags();
  }

  @Test
  void saveJsonFileKo_invalidJsonSchema() {

    FlowToHistoryEntity flowToHistoryEntity =
        mock(FlowToHistoryEntity.class);

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
  void saveJsonFile_shouldUploadWithoutAdditionalOperations() {

    BlobClient blobClient = mock(BlobClient.class);

    FlowToHistoryEntity flowToHistoryEntity =
        mock(FlowToHistoryEntity.class);

    when(flowToHistoryEntity.getIsExternal()).thenReturn(true);

    when(blobContainerClient.getBlobClient(anyString()))
        .thenReturn(blobClient);

    when(blobClient.uploadWithResponse(
        any(BlobParallelUploadOptions.class),
        isNull(),
        eq(Context.NONE)))
        .thenReturn(
            new SimpleResponse<>(
                null,
                201,
                new HttpHeaders(),
                mock(BlockBlobItem.class)));

    FlowBlob flowBlob = validFlowBlob();

    assertDoesNotThrow(
        () -> historyBlobStorageService.saveJsonFile(
            flowBlob, flowToHistoryEntity));

    verify(blobClient).uploadWithResponse(
        any(BlobParallelUploadOptions.class),
        isNull(),
        eq(Context.NONE));

    verify(blobClient, never()).setMetadata(anyMap());
    verify(blobClient, never()).setTags(anyMap());
    verify(blobClient, never()).getTags();
  }
}
