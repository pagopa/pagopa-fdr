package it.gov.pagopa.fdr.repository.entity.flow;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@MongoEntity(collection = "Flow")
public class Flow extends PanacheMongoEntity {
  public Instant created;
  public Instant updated;

  public String idFlow;

  public long totChunk;

  public Set<FlowFile> flowFiles = new LinkedHashSet<>();

  public FlowStatusEnum status;

  public static class FlowFile {
    public Instant received;
    public String fileName;
    public long fileSize;

    public String path;

    public long numberOfChunk;
  }
}
