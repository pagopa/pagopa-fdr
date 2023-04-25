package it.gov.pagopa.fdr.repository.reportingFlow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentEntity;
import java.util.List;

@ProjectionFor(ReportingFlowEntity.class)
public class ReportingFlowPaymentComputedFieldProjection {

  public List<ReportingFlowPaymentEntity> data;
  public Long count;

  public Double sum;
}
