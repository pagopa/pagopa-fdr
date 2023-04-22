package it.gov.pagopa.fdr.repository.reportingFlow.collection;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.collection.model.AbstractReportingFlow;
import org.bson.types.ObjectId;

@MongoEntity(collection = "reporting_flow_revision")
public class ReportingFlowRevision extends AbstractReportingFlow {

  public ObjectId reportingFlowId;
}
