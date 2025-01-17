package it.gov.pagopa.fdr.service.history.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.fdr.repository.enums.PaymentStatusEnum;
import java.time.Instant;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
public class FdrHistoryPaymentEntity {

  private String iuv;

  private String iur;

  private Long index;

  private Double pay;

  @JsonIgnore private Long revision;

  @JsonIgnore private Instant created;

  @JsonIgnore private Instant updated;

  @BsonProperty("pay_status")
  private PaymentStatusEnum payStatus;

  @BsonProperty("pay_date")
  private Instant payDate;

  @JsonIgnore
  @BsonProperty("ref_fdr_id")
  private ObjectId refFdrId;

  @JsonIgnore
  @BsonProperty("ref_fdr")
  private String refFdr;

  @JsonIgnore
  @BsonProperty("ref_fdr_sender_psp_id")
  private String refFdrSenderPspId;

  @JsonIgnore
  @BsonProperty("ref_fdr_revision")
  private Long refFdrRevision;

  @JsonIgnore
  @BsonProperty("ref_fdr_receiver_organization_id")
  private String refFdrReceiverOrganizationId;
}
