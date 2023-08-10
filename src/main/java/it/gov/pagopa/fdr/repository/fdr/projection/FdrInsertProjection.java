package it.gov.pagopa.fdr.repository.fdr.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.fdr.FdrInsertEntity;
import java.time.Instant;
import lombok.Data;

@Data
@ProjectionFor(FdrInsertEntity.class)
public class FdrInsertProjection {

  private String fdr;

  private Sender sender;

  private Long revision;

  private Instant created;
}
