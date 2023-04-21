package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "ReportingFlowRevision")
public class ReportingFlowRevision extends ReportingFlow {}
