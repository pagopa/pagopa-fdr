package it.gov.pagopa.fdr.service.re.model;

import java.time.Instant;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public abstract class ReAbstract {

  private AppVersionEnum appVersion;

  private Instant created;
  private String sessionId;
  private EventTypeEnum eventType;
}
