package it.gov.pagopa.fdr.repository.fdr;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import it.gov.pagopa.fdr.repository.fdr.model.ReceiverEntity;
import it.gov.pagopa.fdr.repository.fdr.model.ReportingFlowStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.model.SenderEntity;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_insert")
public class FdrInsertEntity extends PanacheMongoEntity {

  private Long revision;

  private Instant created;

  private Instant updated;

  @BsonProperty("reporting_flow_name")
  private String reportingFlowName;

  @BsonProperty("reporting_flow_date")
  private Instant reportingFlowDate;

  private SenderEntity sender;

  private ReceiverEntity receiver;

  private String regulation;

  @BsonProperty("regulation_date")
  private Instant regulationDate;

  @BsonProperty("bic_code_pouring_bank")
  private String bicCodePouringBank;

  private ReportingFlowStatusEnumEntity status;

  @BsonProperty("tot_payments")
  private Long totPayments;

  @BsonProperty("sum_payments")
  private Double sumPayments;

  public static PanacheQuery<PanacheMongoEntityBase> findByFlowNameAndPspId(
      String reportingFlowName, String pspId) {
    return find(
        "reporting_flow_name = :flowName and sender.psp_id = :pspId",
        Parameters.with("flowName", reportingFlowName).and("pspId", pspId).map());
  }
}
