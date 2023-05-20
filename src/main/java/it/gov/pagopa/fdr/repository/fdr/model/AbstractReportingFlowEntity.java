package it.gov.pagopa.fdr.repository.fdr.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractReportingFlowEntity extends PanacheMongoEntity {
  private Long revision;

  private Instant created;

  private Instant updated;
  private String reporting_flow_name;
  private Instant reporting_flow_date;

  private SenderEntity sender;

  private ReceiverEntity receiver;

  private String regulation;
  private Instant regulation_date;
  private String bic_code_pouring_bank;

  private ReportingFlowStatusEnumEntity status;

  private Long tot_payments;

  private Double sum_paymnents;

  private Boolean internal_ndp_read;

  private Boolean read;
}
