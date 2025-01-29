package it.gov.pagopa.fdr.service.model.re;

import java.time.Instant;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString
public abstract class ReAbstract {

  private String uniqueId;

  private AppVersionEnum serviceIdentifier;

  private Instant created;
  private String sessionId;
  private EventTypeEnum eventType;

  private String fdr;

  private String pspId;

  private String organizationId;

  private FdrActionEnum fdrAction;
}
