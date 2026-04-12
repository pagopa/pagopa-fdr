package it.gov.pagopa.fdr.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PaymentId implements Serializable {

  @Column(name = "flow_id")
  private Long flowId;

  @Column(name = "index")
  private Long index;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PaymentId paymentId = (PaymentId) o;
    return Objects.equals(flowId, paymentId.flowId) && Objects.equals(index, paymentId.index);
  }

  @Override
  public int hashCode() {
    return Objects.hash(flowId, index);
  }
}

