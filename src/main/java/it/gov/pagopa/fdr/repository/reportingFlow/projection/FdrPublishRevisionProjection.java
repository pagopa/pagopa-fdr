package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPublishEntity;

@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishRevisionProjection {

  public Long revision;
}
