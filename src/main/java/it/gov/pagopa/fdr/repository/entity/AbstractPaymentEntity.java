package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = false)
public class AbstractPaymentEntity extends PanacheEntityBase {

  @Column(name = "iuv")
  public String iuv;

  @Column(name = "iur")
  public String iur;

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
}
