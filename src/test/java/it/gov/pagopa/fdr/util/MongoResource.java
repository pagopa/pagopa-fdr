package it.gov.pagopa.fdr.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class MongoResource implements QuarkusTestResourceLifecycleManager {

  private GenericContainer mongo;

  @SneakyThrows
  @Override
  public Map<String, String> start() {
    mongo =
        new GenericContainer("mongo")
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example")
            .withCommand("--auth");
    mongo.setWaitStrategy(Wait.forLogMessage("(?i).*waiting for connections.*", 1));
    mongo.start();
    Map<String, String> conf = new HashMap<>();
    conf.put(
        "mockserver.mongodb.connection-string",
        "mongodb://root:example@localhost:" + mongo.getMappedPort(27017));
    return conf;
  }

  @Override
  public void stop() {
    mongo.stop();
  }
}
