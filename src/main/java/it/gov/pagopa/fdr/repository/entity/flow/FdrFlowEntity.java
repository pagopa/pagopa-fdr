package it.gov.pagopa.fdr.repository.entity.flow;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_flow")
public class FdrFlowEntity extends PanacheMongoEntity {

  private String name;

  private Long revision;

  @BsonProperty("fdr_date")
  private Instant fdrDate;

  private FlowStatusEnum status;

  private Instant created;

  private Instant updated;

  private Instant published;

  @BsonProperty("tot_amount")
  private Double totAmount;

  @BsonProperty("tot_payments")
  private Long totPayments;

  @BsonProperty("computed_tot_amount")
  private Double computedTotAmount;

  @BsonProperty("computed_tot_payments")
  private Long computedTotPayments;

  private String regulation;

  @BsonProperty("regulation_date")
  private Instant regulationDate;

  @BsonProperty("bic_code_pouring_bank")
  private String bicCodePouringBank;

  private SenderEntity sender;

  private ReceiverEntity receiver;

  @BsonProperty("ref_json")
  private BlobBodyReferenceEntity refJson;

  @BsonProperty("_ts")
  public Instant timestamp;

  public void addOnComputedTotAmount(double value) {
    this.computedTotAmount += value;
  }

  public void addOnComputedTotPayments(int value) {
    this.computedTotPayments += value;
  }

  public static PanacheQuery<PanacheMongoEntityBase> findPageByQuery(
      String query, Sort sort, Parameters parameters) {
    return find(query, sort, parameters.map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByQuery(
      String query, Parameters parameters) {
    return find(query, parameters.map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndPspId(String name, String pspId) {
    return find(
        "name = :name and sender.psp_id = :pspId",
        Parameters.with("name", name).and("pspId", pspId).map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndPspId(
      String name, String pspId, Sort sort) {
    return find(
        "name = :name and sender.psp_id = :pspId",
        sort,
        Parameters.with("name", name).and("pspId", pspId).map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndPspIdAndOrganizationId(
      String name, String pspId, String organizationId) {
    return find(
        "name = :name and sender.psp_id = :pspId and receiver.organization_id = :organizationId",
        Parameters.with("name", name)
            .and("pspId", pspId)
            .and("organizationId", organizationId)
            .map());
  }

  public static PanacheQuery<PanacheMongoEntityBase> findByFdrAndRevAndPspIdAndOrganizationId(
      String name, Long rev, String pspId, String organizationId) {
    return find(
        "name = :name and sender.psp_id = :pspId and revision = :rev and receiver.organization_id ="
            + " :organizationId",
        Parameters.with("name", name)
            .and("pspId", pspId)
            .and("rev", rev)
            .and("organizationId", organizationId)
            .map());
  }
}
