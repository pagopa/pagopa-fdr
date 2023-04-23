package it.gov.pagopa.fdr.repository.reportingFlow.model;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class SenderEntity {

  public SenderTypeEntity type;

  @BsonProperty("id")
  public String id;

  @BsonProperty("psp_id")
  public String pspId;

  @BsonProperty("psp_name")
  public String pspName;

  @BsonProperty("broker_id")
  public String brokerId;

  @BsonProperty("channel_id")
  public String channelId;

  public String password;
}
