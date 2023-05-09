package it.gov.pagopa.fdr.repository.reportingFlow.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.Instant;
import org.bson.types.ObjectId;

public abstract class AbstractReportingFlowPaymentEntity extends PanacheMongoEntity {

  public Long revision;

  public Instant created;

  public Instant updated;

  public String iuv;
  public String iur;

  public Long index;
  public Double pay;

  public PaymentStatusEnumEntity pay_status;

  public Instant pay_date;

  public ObjectId ref_fdr_id;
  public String ref_fdr_reporting_flow_name;
  public String ref_fdr_reporting_sender_psp_id;
}
