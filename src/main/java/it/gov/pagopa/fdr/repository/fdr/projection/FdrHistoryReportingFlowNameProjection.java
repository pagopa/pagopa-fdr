package it.gov.pagopa.fdr.repository.fdr.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@ProjectionFor(FdrHistoryEntity.class)
public class FdrHistoryReportingFlowNameProjection {

  @BsonProperty("reporting_flow_name")
  private String reportingFlowName;

  private Sender sender;

  private Long revision;
}
