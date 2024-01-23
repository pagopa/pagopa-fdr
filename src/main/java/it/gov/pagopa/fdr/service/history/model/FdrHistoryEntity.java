package it.gov.pagopa.fdr.service.history.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.model.FdrStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.model.ReceiverEntity;
import it.gov.pagopa.fdr.repository.fdr.model.SenderEntity;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonProperty;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class FdrHistoryEntity {

  private FdrStatusEnumEntity status;

  private Long revision;

  private Instant created;

  private Instant updated;

  private String fdr;

  @BsonProperty("fdr_date")
  private Instant fdrDate;

  private String regulation;

  @BsonProperty("regulation_date")
  private Instant regulationDate;

  @BsonProperty("bic_code_pouring_bank")
  private String bicCodePouringBank;

  private SenderEntity sender;

  private ReceiverEntity receiver;

  private Instant published;

  @BsonProperty("computed_tot_payments")
  private Long computedTotPayments;

  @BsonProperty("computed_sum_payments")
  private Double computedSumPayments;

  @BsonProperty("tot_payments")
  private Long totPayments;

  @BsonProperty("sum_payments")
  private Double sumPayments;

  @Setter
  private List<FdrHistoryPaymentEntity> paymentList;
}