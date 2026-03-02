package it.gov.pagopa.fdr.test.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers resource for MongoDB.
 * Start a container MongoDB for test which needs Registro Eventi.
 *
 */
public class MongoResource implements QuarkusTestResourceLifecycleManager {

  private MongoDBContainer mongoDBContainer;

  @Override
  public Map<String, String> start() {
    mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0"));
    mongoDBContainer.start();

    Map<String, String> conf = new HashMap<>();
    conf.put("quarkus.mongodb.connection-string", mongoDBContainer.getReplicaSetUrl());
    return conf;
  }

  @Override
  public void stop() {
    // JVM managed, no need to stop manually
    // mongoDBContainer.stop();
  }
}

