package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPublishEntity;

@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishReportingFlowNameProjection {

  public String reporting_flow_name;

  public Sender sender;
}
