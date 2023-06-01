package it.gov.pagopa.fdr.repository.fdr.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishReportingFlowNameProjection {

  @BsonProperty("reporting_flow_name")
  private String reportingFlowName;

  private Sender sender;

  private Long revision;
}
