package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.fdr.model.AbstractReportingFlowEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_publish")
public class FdrPublishEntity extends AbstractReportingFlowEntity {}
