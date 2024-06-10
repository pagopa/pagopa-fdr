package it.gov.pagopa.fdr.service.re;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.re.model.BlobHttpBody;
import it.gov.pagopa.fdr.service.re.model.ReAbstract;
import it.gov.pagopa.fdr.service.re.model.ReInterface;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.EventHub;
import it.gov.pagopa.fdr.util.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReService {

  private final Logger log;

  @ConfigProperty(name = "ehub.re.connect-str")
  String eHubConnectStr;

  @ConfigProperty(name = "ehub.re.name")
  String eHubName;

  @ConfigProperty(name = "blob.re.connect-str")
  String blobConnectStr;

  @ConfigProperty(name = "blob.re.containername")
  String blobContainerName;

  private BlobContainerClient blobContainerClient;

  private final ObjectMapper objectMapper;

  private EventHub eventHub;

  public ReService(Logger log, ObjectMapper objectMapper) {
    this.log = log;
    this.objectMapper = objectMapper;
  }

  public void init() {
    log.infof(
        "EventHub re and blob service init. EventHub name [%s], container name [%s]",
        eHubName, blobContainerName);

    this.eventHub = new EventHub(this.log, this.objectMapper, eHubConnectStr, eHubName);

    BlobServiceClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobConnectStr).buildClient();
    this.blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(blobContainerName);
  }

  @SafeVarargs
  public final <T extends ReAbstract> void sendEvent(T... reList) {
    if (this.eventHub == null || this.blobContainerClient == null) {
      log.debugf(
          "EventHub [%s] or Blob container [%s] NOT INITIALIZED", eHubName, blobContainerName);
    } else {
      List<T> list =
          Arrays.stream(reList)
              .filter(a -> AppConstant.sendReEvent(a.getFdrAction()))
              .peek(
                  re -> {
                    re.setUniqueId(
                        String.format(
                            "%s_%s", dateFormatter.format(re.getCreated()), re.hashCode()));
                    writeBlobIfExist(re);
                  })
              .toList();

      this.eventHub.sendEvent(list);
    }
  }

  private static final String PATTERN_DATE_FORMAT = "yyyy-MM-dd";
  private static final DateTimeFormatter dateFormatter =
      DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT).withZone(ZoneId.systemDefault());

  private <T extends ReAbstract> void writeBlobIfExist(T re) {
    if (re instanceof ReInterface reInterface) {
      String bodyStr = reInterface.getPayload();
      if (bodyStr != null && !bodyStr.isBlank()) {
        String fileName =
            String.format(
                "%s_%s_%s.json.zip",
                re.getSessionId(), re.getFdrAction(), reInterface.getHttpType().name());

        byte[] compressedBody = null;
        try {
          compressedBody = StringUtil.zip(bodyStr);
        } catch (IOException e) {
          log.errorf("Compress json error", e);
          throw new AppException(AppErrorCodeMessageEnum.COMPRESS_JSON);
        }
        BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(compressedBody));
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        blobClient.upload(body);

        BlobHttpBody blobBodyRef =
            BlobHttpBody.builder()
                .storageAccount(blobContainerClient.getAccountName())
                .containerName(blobContainerName)
                .fileName(fileName)
                .fileLength(body.getLength())
                .build();
        reInterface.setBlobBodyRef(blobBodyRef);
      }
    }
  }
}
