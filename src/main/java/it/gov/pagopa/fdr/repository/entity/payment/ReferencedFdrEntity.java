package it.gov.pagopa.fdr.repository.entity.payment;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
public class ReferencedFdrEntity {

  @BsonProperty("id")
  private ObjectId id;

  @BsonProperty("name")
  private String name;

  @BsonProperty("receiver_organization_id")
  private String receiverOrganizationId;

  @BsonProperty("revision")
  private Long revision;

  @BsonProperty("sender_psp_id")
  private String senderPspId;
}
