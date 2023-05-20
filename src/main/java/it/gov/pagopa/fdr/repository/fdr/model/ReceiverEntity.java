package it.gov.pagopa.fdr.repository.fdr.model;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class ReceiverEntity {

  @BsonProperty("id")
  private String id;

  @BsonProperty("ec_id")
  private String ecId;

  @BsonProperty("ec_name")
  private String ecName;
}
