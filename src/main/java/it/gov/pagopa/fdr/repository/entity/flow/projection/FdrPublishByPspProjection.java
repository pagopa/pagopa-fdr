package it.gov.pagopa.fdr.repository.entity.flow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.entity.flow.FdrPublishEntity;
import java.time.Instant;
import lombok.Data;

@Data
@ProjectionFor(FdrPublishEntity.class)
public class FdrPublishByPspProjection {

  private String fdr;

  private ReceiverProjection receiverProjection;

  private Long revision;

  private Instant published;
}
