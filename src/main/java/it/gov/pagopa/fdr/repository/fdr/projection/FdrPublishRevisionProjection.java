package it.gov.pagopa.fdr.repository.fdr.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;

@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishRevisionProjection {

  public Long revision;
}
