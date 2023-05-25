package it.gov.pagopa.fdr.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoResource implements QuarkusTestResourceLifecycleManager {

  private GenericContainer mongo;

  @SneakyThrows
  @Override
  public Map<String, String> start() {
    mongo = new MongoDBContainer(DockerImageName.parse("mongo:latest")).withExposedPorts(27017);
    mongo.start();
    Map<String, String> conf = new HashMap<>();
    conf.put(
        "mockserver.mongodb.connection-string",
        "mongodb://localhost:" + mongo.getMappedPort(27017));
    return conf;
  }

  @Override
  public void stop() {
    mongo.stop();
  }
}
