package it.gov.pagopa.fdr.repository.fdr.model;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class SenderEntity {

  private SenderTypeEnumEntity type;

  @BsonProperty("id")
  private String id;

  @BsonProperty("psp_id")
  private String pspId;

  @BsonProperty("psp_name")
  private String pspName;

  @BsonProperty("broker_id")
  private String brokerId;

  @BsonProperty("channel_id")
  private String channelId;

  private String password;
}
