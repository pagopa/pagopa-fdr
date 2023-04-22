package it.gov.pagopa.fdr.repository.reportingFlow.collection;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.collection.model.AbstractReportingFlow;

@MongoEntity(collection = "reporting_flow")
public class ReportingFlow extends AbstractReportingFlow {}
