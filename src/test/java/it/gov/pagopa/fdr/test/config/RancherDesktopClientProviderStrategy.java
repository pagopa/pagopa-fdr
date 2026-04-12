package it.gov.pagopa.fdr.test.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import org.testcontainers.dockerclient.DockerClientProviderStrategy;
import org.testcontainers.dockerclient.TransportConfig;

import java.io.File;
import java.net.URI;

/**
 * Custom Testcontainers strategy for Rancher Desktop.
 * Create the DockerClient for API version 1.41 using docker-java-core,
 * in order to avoid "client version 1.32 is too old" problem.
 */
public class RancherDesktopClientProviderStrategy extends DockerClientProviderStrategy {

  private static final String RANCHER_SOCKET_PATH =
      System.getProperty("user.home") + "/.rd/docker.sock";
  private static final String RANCHER_DOCKER_HOST = "unix://" + RANCHER_SOCKET_PATH;

  @Override
  public String getDescription() {
    return "Rancher Desktop (API 1.41)";
  }

  @Override
  protected boolean isApplicable() {
    return new File(RANCHER_SOCKET_PATH).exists();
  }

  @Override
  protected int getPriority() {
    return Integer.MAX_VALUE;
  }

  @Override
  public TransportConfig getTransportConfig() {
    return TransportConfig.builder()
        .dockerHost(URI.create(RANCHER_DOCKER_HOST))
        .build();
  }

  @Override
  public DockerClient getDockerClient() {
    DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost(RANCHER_DOCKER_HOST)
        .withDockerTlsVerify(false)
        .withApiVersion("1.41")
        .build();

    ZerodepDockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create(RANCHER_DOCKER_HOST))
        .build();

    return DockerClientImpl.getInstance(config, httpClient);
  }
}
