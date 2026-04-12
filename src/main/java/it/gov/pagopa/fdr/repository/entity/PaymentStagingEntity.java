package it.gov.pagopa.fdr.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_staging")
public class PaymentStagingEntity extends AbstractPaymentEntity {

  @EmbeddedId
  private PaymentStagingId id;
}
