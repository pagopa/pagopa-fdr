package it.gov.pagopa.fdr.repository.entity.flow.projection;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class ReceiverProjection {

  @BsonProperty("organization_id")
  private String organizationId;
}
