package it.gov.pagopa.fdr.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoResource implements QuarkusTestResourceLifecycleManager {

  private MongoDBContainer mongo;

  @SneakyThrows
  @Override
  public Map<String, String> start() {
    mongo =
        new MongoDBContainer(DockerImageName.parse("mongo"))
            .withExposedPorts(27017)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example");
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
