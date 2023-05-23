package it.gov.pagopa.fdr.repository.fdr.projection;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class Sender {

  @BsonProperty("psp_id")
  private String pspId;
}
