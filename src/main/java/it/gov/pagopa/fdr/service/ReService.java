package it.gov.pagopa.fdr.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReService {

  /*
  private final Logger log;

  @ConfigProperty(name = "ehub.re.connect-str")
  String eHubConnectStr;

  @ConfigProperty(name = "ehub.re.name")
  String eHubName;

  @ConfigProperty(name = "blob.re.connect-str")
  String blobConnectStr;

  @ConfigProperty(name = "blob.re.containername")
  String blobContainerName;

  private EventHubProducerClient producer;

  private BlobContainerClient blobContainerClient;

  private final ObjectMapper objectMapper;

  public ReService(Logger log, ObjectMapper objectMapper) {
    this.log = log;
    this.objectMapper = objectMapper;
  }

  public void init() {
    log.infof(
        "EventHub re and blob service init. EventHub name [%s], container name [%s]",
        eHubName, blobContainerName);

    this.producer =
        new EventHubClientBuilder()
            .connectionString(eHubConnectStr, eHubName)
            .buildProducerClient();

    BlobServiceClient blobServiceClient =
        new BlobServiceClientBuilder().connectionString(blobConnectStr).buildClient();
    this.blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(blobContainerName);
  }

  @SafeVarargs
  public final <T extends ReAbstract> void sendEvent(T... reList) {
    if (this.producer == null || this.blobContainerClient == null) {
      log.debugf(
          "EventHub re [%s] or Blob container [%s] NOT INITIALIZED", eHubName, blobContainerName);
    } else {
      List<EventData> allEvents =
          Arrays.stream(reList)
              .filter(a -> AppConstant.sendReEvent(a.getFdrAction()))
              .map(
                  re -> {
                    re.setUniqueId(
                        String.format(
                            "%s_%s", dateFormatter.format(re.getCreated()), re.hashCode()));
                    writeBlobIfExist(re);
                    try {
                      log.debugf("EventHub name [%s] send message: %s", eHubName, re.toString());
                      return new EventData(objectMapper.writeValueAsString(re));
                    } catch (JsonProcessingException e) {
                      log.errorf("Producer SDK Azure RE event error", e);
                      throw new AppException(AppErrorCodeMessageEnum.EVENT_HUB_RE_PARSE_JSON);
                    }
                  })
              .toList();
      if (!allEvents.isEmpty()) {
        publishEvents(allEvents);
      }
    }
  }

  private static final String PATTERN_DATE_FORMAT = "yyyy-MM-dd";
  private static final DateTimeFormatter dateFormatter =
      DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT).withZone(ZoneId.systemDefault());

  public <T extends ReAbstract> void writeBlobIfExist(T re) {
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

  public void publishEvents(List<EventData> allEvents) {
    // create a batch
    EventDataBatch eventDataBatch = producer.createBatch();

    for (EventData eventData : allEvents) {
      // try to add the event from the array to the batch
      if (!eventDataBatch.tryAdd(eventData)) {
        // if the batch is full, send it and then create a new batch
        producer.send(eventDataBatch);
        eventDataBatch = producer.createBatch();

        // Try to add that event that couldn't fit before.
        if (!eventDataBatch.tryAdd(eventData)) {
          throw new AppException(
              AppErrorCodeMessageEnum.EVENT_HUB_RE_TOO_LARGE, eventDataBatch.getMaxSizeInBytes());
        }
      }
    }
    // send the last batch of remaining events
    if (eventDataBatch.getCount() > 0) {
      producer.send(eventDataBatch);
    }
  }
   */
}
