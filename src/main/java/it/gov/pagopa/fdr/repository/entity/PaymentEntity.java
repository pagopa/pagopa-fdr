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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment")
public class PaymentEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq_gen")
  @SequenceGenerator(
      name = "payment_seq_gen",
      sequenceName = "payment_sequence",
      allocationSize = 1)
  @Column(name = "id", nullable = false, updatable = false)
  public Long id;

  @Column(name = "flow_id")
  public Long flowId;

  @Column(name = "iuv")
  public String iuv;

  @Column(name = "iur")
  public String iur;

  @Column(name = "index")
  public Long index;

  @Column(name = "amount")
  public BigDecimal amount;

  @Column(name = "pay_date")
  public Instant payDate;

  @Column(name = "pay_status")
  public String payStatus;

  @Column(name = "transfer_id")
  public Long transferId;

  @Column(name = "created")
  public Instant created;

  @Column(name = "updated")
  public Instant updated;

  // @ManyToOne(fetch = FetchType.LAZY)
  // @JoinColumn(name = "flow_id", referencedColumnName = "id")
  // public FlowEntity flow;

  public static PanacheQuery<PaymentEntity> findPageByQuery(
      String query, Sort sort, Parameters parameters) {
    return find(query, sort, parameters.map());
  }

  public void exportInPreparedStatement(PreparedStatement preparedStatement) throws SQLException {

    preparedStatement.setLong(1, this.flowId);
    preparedStatement.setString(2, this.iuv);
    preparedStatement.setString(3, this.iur);
    preparedStatement.setLong(4, this.index);
    preparedStatement.setBigDecimal(5, this.amount);
    preparedStatement.setTimestamp(6, this.payDate != null ? Timestamp.from(this.payDate) : null);
    preparedStatement.setString(7, this.payStatus);
    preparedStatement.setLong(8, this.transferId);
    preparedStatement.setTimestamp(9, this.created != null ? Timestamp.from(this.created) : null);
    preparedStatement.setTimestamp(10, this.updated != null ? Timestamp.from(this.updated) : null);
  }
}
