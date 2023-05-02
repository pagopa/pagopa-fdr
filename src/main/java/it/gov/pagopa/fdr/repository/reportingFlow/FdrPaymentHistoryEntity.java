package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.model.AbstractReportingFlowPaymentEntity;

@MongoEntity(collection = "fdr_payment_history")
public class FdrPaymentHistoryEntity extends AbstractReportingFlowPaymentEntity {}
