package it.gov.pagopa.fdr.storage;

import static it.gov.pagopa.fdr.test.util.TestUtil.validFlowBlob;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import jakarta.inject.Inject;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class HistoryBlobStorageServiceTest {

  @InjectMock BlobContainerClient blobContainerClient;

  @Inject HistoryBlobStorageService historyBlobStorageService;

  @Test
  void saveJsonFileOk() throws IOException {
    BlobClient blobClient = Mockito.mock(BlobClient.class);
    when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);

    historyBlobStorageService.saveJsonFile(validFlowBlob());
    verify(blobClient).upload(any(BinaryData.class), anyBoolean());
  }

  @Test
  void saveJsonFileKo() {
    BlobClient blobClient = Mockito.mock(BlobClient.class);
    when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
    FlowBlob build = FlowBlob.builder().build();
    assertThrows(
        Exception.class,
        () -> {
          historyBlobStorageService.saveJsonFile(build);
        });
  }
}
