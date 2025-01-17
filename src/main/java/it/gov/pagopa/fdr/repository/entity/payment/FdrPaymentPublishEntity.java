package it.gov.pagopa.fdr.repository.entity.payment;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.enums.PaymentStatusEnum;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_payment_publish")
public class FdrPaymentPublishEntity extends PanacheMongoEntity {

  private Long index;

  private Long revision;

  private Instant created;

  private Instant updated;

  private String iuv;
  private String iur;

  private Long idTransfer;
  private Double pay;

  @BsonProperty("pay_status")
  private PaymentStatusEnum payStatus;

  @BsonProperty("pay_date")
  private Instant payDate;

  @BsonProperty("ref_fdr_id")
  private ObjectId refFdrId;

  @BsonProperty("ref_fdr")
  private String refFdr;

  @BsonProperty("ref_fdr_sender_psp_id")
  private String refFdrSenderPspId;

  @BsonProperty("ref_fdr_revision")
  private Long refFdrRevision;

  @BsonProperty("ref_fdr_receiver_organization_id")
  private String refFdrReceiverOrganizationId;

  public static void persistFdrPaymentHistoryEntities(
      List<FdrPaymentPublishEntity> fdrPaymentHistoryEntities) {
    persist(fdrPaymentHistoryEntities);
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndRevAndPspIdAndOrganizationId(
      String fdr, Long rev, String pspId, String organizationId, Sort sort) {
    return find(
        "ref_fdr = :fdr and ref_fdr_revision = :rev and ref_fdr_sender_psp_id = :pspId and"
            + " ref_fdr_receiver_organization_id = :organizationId",
        sort,
        Parameters.with("fdr", fdr)
            .and("rev", rev)
            .and("pspId", pspId)
            .and("organizationId", organizationId)
            .map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByPspAndIuvIur(
      String psp, String iuv, String iur, Instant createdFrom, Instant createdTo, Sort sort) {
    String query = "ref_fdr_sender_psp_id = :psp";
    Parameters params = new Parameters().and("psp", psp);
    if (iuv != null && iur != null) {
      query += " and iuv = :iuv and iur = :iur";
      params.and("iuv", iuv).and("iur", iur);
    } else if (iuv != null) {
      query += " and iuv = :iuv";
      params.and("iuv", iuv);
    } else if (iur != null) {
      query += " and iur = :iur";
      params.and("iur", iur);
    }
    if (createdFrom != null && createdTo != null) {
      query += " and created >= :createdFrom and created <= :createdTo";
      params.and("createdFrom", createdFrom).and("createdTo", createdTo);
    } else if (createdFrom != null) {
      query += " and created >= :createdFrom";
      params.and("createdFrom", createdFrom);
    } else if (createdTo != null) {
      query += " and created <= :createdTo";
      params.and("createdTo", createdTo);
    }
    return find(query, sort, params);
  }

  public static void persistFdrPaymentPublishEntities(
      List<FdrPaymentPublishEntity> fdrPaymentPublishEntities) {
    persist(fdrPaymentPublishEntities);
  }
}
