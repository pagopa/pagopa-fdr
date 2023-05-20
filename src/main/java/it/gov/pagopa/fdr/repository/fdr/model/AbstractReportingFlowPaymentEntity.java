package it.gov.pagopa.fdr.repository.fdr.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractReportingFlowPaymentEntity extends PanacheMongoEntity {

  private Long revision;

  private Instant created;

  private Instant updated;

  private String iuv;
  private String iur;

  private Long index;
  private Double pay;

  private PaymentStatusEnumEntity pay_status;

  private Instant pay_date;

  private ObjectId ref_fdr_id;
  private String ref_fdr_reporting_flow_name;
  private String ref_fdr_reporting_sender_psp_id;
}
