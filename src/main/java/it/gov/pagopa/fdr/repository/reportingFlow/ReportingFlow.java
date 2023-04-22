package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "reporting_flow")
public class ReportingFlow extends AbstractReportingFlow {}
