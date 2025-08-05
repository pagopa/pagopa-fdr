package it.gov.pagopa.fdr.config;

import com.azure.storage.blob.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class BlobClientProducer {

  @ConfigProperty(name = "blob.history.connect-str")
  String blobHistoryConnectionStr;

  @ConfigProperty(name = "blob.history.containername")
  String blobHistoryContainerName;

  @ConfigProperty(name = "blob.re.connect-str")
  String blobReConnectionStr;

  @ConfigProperty(name = "blob.re.name")
  String blobReContainerName;

  @Produces
  @ApplicationScoped
  public BlobContainerAsyncClient createBlobContainerAsyncClient() {

    BlobServiceAsyncClient blobServiceClient =
        new BlobServiceClientBuilder()
            .connectionString(blobHistoryConnectionStr)
            .buildAsyncClient();
    return blobServiceClient.getBlobContainerAsyncClient(blobHistoryContainerName);
  }

  @Produces
  @ApplicationScoped
  public BlobContainerClient createBlobContainerClient() {

    BlobServiceClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobReConnectionStr).buildClient();
    return blobServiceClient.getBlobContainerClient(blobReContainerName);
  }
}
