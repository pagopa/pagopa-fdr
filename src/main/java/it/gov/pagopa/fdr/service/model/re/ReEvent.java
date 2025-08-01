package it.gov.pagopa.fdr.service.model.re;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ReEvent {

  private AppVersionEnum serviceIdentifier;

  private Instant created;

  private String sessionId;

  private EventTypeEnum eventType;

  private String fdr;

  private String pspId;

  private String organizationId;

  private FdrActionEnum fdrAction;

  private FdrStatusEnum fdrStatus;

  private Long revision;

  private String httpMethod;

  private String httpUrl;

  private String reqPayload;

  private String resPayload;

  private BlobHttpBody reqBodyRef;

  private BlobHttpBody resBodyRef;

  private Map<String, List<String>> header;
}
