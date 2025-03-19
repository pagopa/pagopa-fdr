package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "flow_to_history")
public class FlowToHistoryEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flow_to_history_seq_gen")
  @SequenceGenerator(
      name = "flow_to_history_seq_gen",
      sequenceName = "flow_to_history_sequence",
      allocationSize = 1)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "psp_id")
  private String pspId;

  @Column(name = "name")
  private String name;

  @Column(name = "revision")
  private Long revision;

  @Column(name = "is_external")
  private Boolean isExternal;

  @Column(name = "created")
  private Instant created;

  @Column(name = "last_execution")
  private Instant lastExecution;

  @Column(name = "retries")
  private Integer retries;
}
