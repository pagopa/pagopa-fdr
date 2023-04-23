package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.AbstractReportingFlowPaymentEntity;

@MongoEntity(collection = "reporting_flow_payment")
public class ReportingFlowPaymentEntity extends AbstractReportingFlowPaymentEntity {}
