package it.gov.pagopa.fdr.repository.fdr.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import lombok.Data;

@Data
@ProjectionFor(FdrHistoryEntity.class)
public class FdrHistoryProjection {

  private String fdr;

  private Sender sender;

  private Long revision;
}
