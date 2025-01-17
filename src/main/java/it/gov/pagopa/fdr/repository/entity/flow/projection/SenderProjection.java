package it.gov.pagopa.fdr.repository.entity.flow.projection;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class SenderProjection {

  @BsonProperty("psp_id")
  private String pspId;
}
