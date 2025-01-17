package it.gov.pagopa.fdr.repository.entity.flow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.entity.flow.FdrPublishEntity;
import java.time.Instant;
import lombok.Data;

@Data
@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishProjection {

  private String fdr;

  private SenderProjection senderProjection;

  private Long revision;

  private Instant published;
}
