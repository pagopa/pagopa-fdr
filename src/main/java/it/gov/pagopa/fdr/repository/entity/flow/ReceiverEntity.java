package it.gov.pagopa.fdr.repository.entity.flow;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class ReceiverEntity {

  @BsonProperty("id")
  private String id;

  @BsonProperty("organization_id")
  private String organizationId;

  @BsonProperty("organization_name")
  private String organizationName;
}
