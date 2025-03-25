package it.gov.pagopa.fdr.storage;

import static it.gov.pagopa.fdr.test.util.TestUtil.validFlowBlob;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import jakarta.inject.Inject;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

@QuarkusTest
class HistoryBlobStorageServiceTest {

  @InjectMock BlobContainerAsyncClient blobContainerAsyncClient;

  @Inject HistoryBlobStorageService historyBlobStorageService;

  @Test
  void saveJsonFileOk() throws IOException {
    BlobAsyncClient blobClient = Mockito.mock(BlobAsyncClient.class);
    when(blobClient.upload(any(BinaryData.class), anyBoolean()))
        .thenReturn(
            new Mono<>() {
              @Override
              public void subscribe(CoreSubscriber<? super BlockBlobItem> coreSubscriber) {
                // do nothing
              }
            });
    when(blobClient.setMetadata(any())).thenReturn(null);
    when(blobContainerAsyncClient.getBlobAsyncClient(anyString())).thenReturn(blobClient);

    historyBlobStorageService.saveJsonFile(validFlowBlob());
    verify(blobClient).upload(any(BinaryData.class), anyBoolean());
  }

  @Test
  void saveJsonFileKo() {
    BlobAsyncClient blobClient = Mockito.mock(BlobAsyncClient.class);
    when(blobContainerAsyncClient.getBlobAsyncClient(anyString())).thenReturn(blobClient);
    FlowBlob build = FlowBlob.builder().build();
    assertThrows(
        Exception.class,
        () -> {
          historyBlobStorageService.saveJsonFile(build);
        });
  }
}
