package it.gov.pagopa.fdr.repository.fdr.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import lombok.Data;

@Data
@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishReportingFlowNameProjection {

  private String reportingFlowName;

  private Sender sender;
}
