package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.AbstractReportingFlowEntity;
import org.bson.types.ObjectId;

@MongoEntity(collection = "reporting_flow_revision")
public class ReportingFlowRevisionEntity extends AbstractReportingFlowEntity {

  public ObjectId reporting_flow_id;
}
