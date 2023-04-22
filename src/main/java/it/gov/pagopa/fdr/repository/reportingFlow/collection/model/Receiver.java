package it.gov.pagopa.fdr.repository.reportingFlow.collection.model;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class Receiver {
  @BsonProperty("id")
  private String id;

  private String idEc;
  private String nameEc;
}
