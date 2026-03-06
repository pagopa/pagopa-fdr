package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_full_view")
@Immutable
public class PaymentFullViewEntity extends AbstractPaymentEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "flow_id", referencedColumnName = "id", insertable = false, updatable = false)
  public FlowEntity flow;

  public static PanacheQuery<PaymentFullViewEntity> findPageByQuery(
          String query, Parameters parameters) {
    return find(query, parameters.map());
  }

}
