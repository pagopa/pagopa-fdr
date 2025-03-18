package it.gov.pagopa.fdr.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
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
  public BlobContainerClient createBlobContainerClient() {
    BlobServiceClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobConnectionsStr).buildClient();

    return blobServiceClient.getBlobContainerClient(blobContainerName);
  }
}
