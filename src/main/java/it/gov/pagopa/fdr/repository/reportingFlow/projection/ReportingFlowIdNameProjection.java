package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;
import org.bson.types.ObjectId;

@ProjectionFor(ReportingFlowEntity.class)
public class ReportingFlowIdNameProjection {

  public ObjectId id;
  public String reporting_flow_name;
}
