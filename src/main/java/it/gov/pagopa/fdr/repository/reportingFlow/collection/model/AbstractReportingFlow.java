package it.gov.pagopa.fdr.repository.reportingFlow.collection.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import java.time.Instant;
import java.util.List;

public abstract class AbstractReportingFlow extends PanacheMongoEntity {
  public Long revision;
  public Instant created;
  public Instant updated;

  public String reportingFlow;
  public Instant dateReportingFlow;

  public Sender sender;
  public Receiver receiver;

  public String regulation;
  public Instant dateRegulation;

  public String bicCodePouringBank;

  public List<Pagamento> payments;

  public ReportingFlowStatusEnum status;
}
