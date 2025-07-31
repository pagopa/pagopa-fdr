package it.gov.pagopa.fdr.config;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class BlobClientProducer {

  @ConfigProperty(name = "blob.history.connect-str")
  String blobConnectionsStr;

  @ConfigProperty(name = "blob.history.containername")
  String blobContainerName;

  @Produces
  @ApplicationScoped
  public BlobContainerAsyncClient createBlobContainerClient() {

    BlobServiceAsyncClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobConnectionsStr).buildAsyncClient();
    return blobServiceClient.getBlobContainerAsyncClient(blobContainerName);
  }
}
