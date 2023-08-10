package it.gov.pagopa.fdr.repository.fdr.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import java.time.Instant;
import lombok.Data;

@Data
@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishProjection {

  private String fdr;

  private Sender sender;

  private Long revision;

  private Instant published;
}
