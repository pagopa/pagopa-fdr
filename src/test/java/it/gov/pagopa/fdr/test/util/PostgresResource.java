package it.gov.pagopa.fdr.test.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.HashMap;
import java.util.Map;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {


  private PostgreSQLContainer postgres;

  //@SneakyThrows
  @Override
  public Map<String, String> start() {

    postgres = (PostgreSQLContainer) new PostgreSQLContainer(DockerImageName.parse("postgres:latest")).withCopyFileToContainer(
            MountableFile.forClasspathResource("/db-init.sql"),
            "/docker-entrypoint-initdb.d/");
    postgres.start();
    Map<String, String> conf = new HashMap<>();
    conf.put(
            "quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
    conf.put(
            "quarkus.datasource.username", "test");
    conf.put(
            "quarkus.datasource.password","test");
    conf.put(
            "quarkus.datasource.jdbc.max-size","5");
    return conf;

  }
  @Override
  public void stop() {
    // JVM managed, no need to stop manually
    //postgres.stop();
  }



}
