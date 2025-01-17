package it.gov.pagopa.fdr.repository.entity.flow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.entity.flow.FdrInsertEntity;
import java.time.Instant;
import lombok.Data;

@Data
@ProjectionFor(FdrInsertEntity.class)
public class FdrInsertProjection {

  private String fdr;

  private ReceiverProjection receiverProjection;

  private Long revision;

  private Instant created;
}
