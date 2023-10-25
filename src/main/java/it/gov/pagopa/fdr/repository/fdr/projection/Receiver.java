package it.gov.pagopa.fdr.repository.fdr.projection;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class Receiver {

  @BsonProperty("organization_id")
  private String organizationId;
}
