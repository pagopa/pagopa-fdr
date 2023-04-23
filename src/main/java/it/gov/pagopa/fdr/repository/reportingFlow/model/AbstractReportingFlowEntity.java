package it.gov.pagopa.fdr.repository.reportingFlow.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.Instant;

public abstract class AbstractReportingFlowEntity extends PanacheMongoEntity {
  public Long revision;
  public Instant created;
  public Instant updated;

  public String reporting_flow_name;

  public Instant reporting_flow_date;

  public SenderEntity sender;
  public ReceiverEntity receiver;

  public String regulation;

  public Instant regulation_date;

  public String bic_code_pouring_bank;

  public ReportingFlowStatusEnumEntity status;
}
