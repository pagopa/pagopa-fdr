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

  @BsonProperty("psp_broker_id")
  private String pspBrokerId;

  @BsonProperty("channel_id")
  private String channelId;

  private String password;
}
