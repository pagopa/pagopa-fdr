package it.gov.pagopa.fdr.repository.reportingFlow;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@MongoEntity(collection = "ReportingFlow")
public class ReportingFlow extends PanacheMongoEntity {
  public Instant created;
  public Instant updated;

  public String reportingFlow;
  public Instant dateReportingFlow;

  private Sender sender;
  private Receiver receiver;

  private String regulation;
  private Instant dateRegulation;

  private Optional<String> bicCodePouringBank;

  private List<Pagamento> payments;

  public ReportingFlowStatusEnum status;
}
