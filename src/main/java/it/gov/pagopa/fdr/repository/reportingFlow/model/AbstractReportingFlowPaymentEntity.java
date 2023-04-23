package it.gov.pagopa.fdr.repository.reportingFlow.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.math.BigDecimal;
import java.time.Instant;

public abstract class AbstractReportingFlowPaymentEntity extends PanacheMongoEntity {
  public String iuv;
  public String iur;
  public Long index;
  public BigDecimal payed;

  public PaymentStatusEntity status;

  public Instant payed_date;

  public String reporting_flow_name;
}
