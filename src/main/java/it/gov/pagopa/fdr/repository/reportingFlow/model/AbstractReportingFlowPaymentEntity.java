package it.gov.pagopa.fdr.repository.reportingFlow.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.math.BigDecimal;
import java.time.Instant;
import org.bson.types.ObjectId;

public abstract class AbstractReportingFlowPaymentEntity extends PanacheMongoEntity {

  public Long revision;
  public Instant created;
  public Instant updated;

  public String iuv;
  public String iur;
  public Long index;
  public BigDecimal pay;

  public PaymentStatusEntity pay_status;

  public Instant pay_date;

  public ObjectId reporting_flow_id;
  public String reporting_flow_name;

  public ReportingFlowPaymentStatusEnumEntity status;
}
