package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.AbstractReportingFlowPaymentEntity;
import org.bson.types.ObjectId;

@MongoEntity(collection = "reporting_flow_payment_revision")
public class ReportingFlowPaymentRevisionEntity extends AbstractReportingFlowPaymentEntity {

  public ObjectId ref_reporting_flow_payment_id;
}
