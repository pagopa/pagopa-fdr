package it.gov.pagopa.fdr.repository.entity.flow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.entity.flow.FdrPublishEntity;
import lombok.Data;

@Data
@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishRevisionProjection {

  private Long revision;
}
