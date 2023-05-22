package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.fdr.model.AbstractReportingFlowPaymentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_payment_publish")
public class FdrPaymentPublishEntity extends AbstractReportingFlowPaymentEntity {}
