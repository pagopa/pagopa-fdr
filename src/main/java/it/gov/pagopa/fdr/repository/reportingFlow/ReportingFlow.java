package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.Instant;
import java.util.List;

@MongoEntity(collection = "ReportingFlow")
public class ReportingFlow extends PanacheMongoEntity {
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
