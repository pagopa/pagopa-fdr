package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;

@ProjectionFor(ReportingFlowEntity.class)
public class ReportingFlowNameProjection {

  public String reporting_flow_name;
}
