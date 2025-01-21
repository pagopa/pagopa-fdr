package it.gov.pagopa.fdr.repository.entity.flow.projection;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
@ProjectionFor(FdrFlowEntity.class)
public class FdrFlowIdProjection {

  @BsonProperty("_id")
  private ObjectId id;
}
