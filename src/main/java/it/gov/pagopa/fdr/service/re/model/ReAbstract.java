package it.gov.pagopa.fdr.service.re.model;

import java.io.Serializable;
import java.time.Instant;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString
// @Jacksonized
public abstract class ReAbstract implements Serializable {

  private AppVersionEnum appVersion;

  private Instant created;
  private String sessionId;
  private EventTypeEnum eventType;

  private String flowName;

  private String pspId;
}
