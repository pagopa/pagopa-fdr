package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import org.bson.types.ObjectId;

@ProjectionFor(ReportingFlow.class)
public class ReportingFlowId {
  public ObjectId id;
}
