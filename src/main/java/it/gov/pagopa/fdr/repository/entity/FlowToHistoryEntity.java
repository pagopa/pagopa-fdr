package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import it.gov.pagopa.fdr.repository.enums.FlowToHistoryStatusEnum;
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
  public String pspId;

  @Column(name = "name")
  public String name;

  @Column(name = "revision")
  public Long revision;

  @Column(name = "is_external")
  public Boolean isExternal;

  @Column(name = "created")
  public Instant created;

  @Column(name = "last_execution")
  public Instant lastExecution;

  @Column(name = "retries")
  public Integer retries;

  @Column(name = "generation_process")
  public FlowToHistoryStatusEnum generationProcess;

  @Column(name = "flow_metadata_persistence")
  public FlowToHistoryStatusEnum flowMetadataPersistence;

  @Column(name = "payment_metadata_persistence")
  public FlowToHistoryStatusEnum paymentMetadataPersistence;

  @Column(name = "last_partition")
  public Integer lastPartition;

  @Column(name = "qi_notification")
  public FlowToHistoryStatusEnum qiNotification;

  @Column(name = "fase1_notification")
  public FlowToHistoryStatusEnum fase1Notification;
}
