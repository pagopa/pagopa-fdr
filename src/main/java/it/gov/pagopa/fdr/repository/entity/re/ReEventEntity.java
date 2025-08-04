package it.gov.pagopa.fdr.repository.entity.re;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "events")
public class ReEventEntity extends PanacheMongoEntity {

  @BsonProperty("PartitionKey")
  private String partitionKey;

  private String uniqueId;

  private String fdr;

  private String fdrAction;

  private String serviceIdentifier;

  private String created;

  private String sessionId;

  private String eventType;

  private String pspId;

  private String organizationId;

  private String fdrStatus;

  private Integer revision;

  private String httpMethod;

  private String httpUrl;

  private BlobRefEntity reqBlobBodyRef;

  private BlobRefEntity resBlobBodyRef;

  private Map<String, List<String>> header;
}
