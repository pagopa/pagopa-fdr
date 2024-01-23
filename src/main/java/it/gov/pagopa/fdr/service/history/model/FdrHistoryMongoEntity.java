package it.gov.pagopa.fdr.service.history.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.fdr.repository.fdr.model.FdrStatusEnumEntity;
import it.gov.pagopa.fdr.repository.fdr.model.ReceiverEntity;
import it.gov.pagopa.fdr.repository.fdr.model.SenderEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonProperty;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "fdr_history")
public class FdrHistoryMongoEntity extends PanacheMongoEntity {

  private Long revision;

  private Instant created;

  private Instant updated;

  private Instant published;

  private String fdr;

  @BsonProperty("fdr_date")
  private Instant fdrDate;

  private SenderEntity sender;

  private ReceiverEntity receiver;

  private String bicCodePouringBank;

  private FdrStatusEnumEntity status;

  private FdrHistoryEntity jsonFile;
}