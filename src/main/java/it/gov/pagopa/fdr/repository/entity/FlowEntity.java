package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "flow")
public class FlowEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flow_seq_gen")
  @SequenceGenerator(name = "flow_seq_gen", sequenceName = "flow_sequence", allocationSize = 1)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "name")
  public String name;

  @Column(name = "date")
  public Instant date;

  @Column(name = "revision")
  public Long revision;

  @Column(name = "status")
  public String status;

  @Column(name = "is_latest")
  public Boolean isLatest;

  @Column(name = "psp_domain_id")
  public String pspDomainId; // domainId

  @Column(name = "org_domain_id")
  public String orgDomainId; // domainId

  @Column(name = "tot_amount")
  public BigDecimal totAmount;

  @Column(name = "tot_payments")
  public Long totPayments;

  @Column(name = "computed_tot_amount")
  public BigDecimal computedTotAmount;

  @Column(name = "computed_tot_payments")
  public Long computedTotPayments;

  @Column(name = "regulation")
  public String regulation;

  @Column(name = "regulation_date")
  public Instant regulationDate;

  @Column(name = "sender_id")
  public String senderId;

  @Column(name = "sender_psp_broker_id")
  public String senderPspBrokerId;

  @Column(name = "sender_channel_id")
  public String senderChannelId;

  @Column(name = "sender_password")
  public String senderPassword;

  @Column(name = "sender_psp_name")
  public String senderPspName;

  @Column(name = "sender_type")
  public String senderType;

  @Column(name = "receiver_id")
  public String receiverId;

  @Column(name = "receiver_organization_name")
  public String receiverOrganizationName;

  @Column(name = "bic_code_pouring_bank")
  public String bicCodePouringBank;

  @Column(name = "created")
  public Instant created;

  @Column(name = "updated")
  public Instant updated;

  @Column(name = "published")
  public Instant published;

  public void addOnComputedTotAmount(double value) {
    this.computedTotAmount = this.computedTotAmount.add(BigDecimal.valueOf(value));
  }

  public void addOnComputedTotPayments(int value) {
    this.computedTotPayments += value;
  }

  public static PanacheQuery<FlowEntity> findPageByQuery(
      String query, Sort sort, Parameters parameters) {
    return find(query, sort, parameters.map());
  }
}
