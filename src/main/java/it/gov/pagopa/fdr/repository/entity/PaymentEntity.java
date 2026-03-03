package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class PaymentEntity extends AbstractPaymentEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "flow_id", referencedColumnName = "id", insertable = false, updatable = false)
  public FlowEntity flow;

  public static PanacheQuery<PaymentEntity> findPageByQuery(
      String query, Parameters parameters) {
    return find(query, parameters.map());
  }
}
