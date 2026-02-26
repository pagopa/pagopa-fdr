package it.gov.pagopa.fdr.test.config;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * JUnit listener will configure Testcontainers before any tests run.
 * It is automatically loaded via ServiceLoader from META-INF/services.
 */
public class TestcontainersConfigurationListener implements LauncherSessionListener {

  @Override
  public void launcherSessionOpened(LauncherSession session) {
    String socketPath = System.getProperty("user.home") + "/.rd/docker.sock";
    String dockerHost = "unix://" + socketPath;

    System.setProperty("DOCKER_HOST", dockerHost);
    System.setProperty("docker.host", dockerHost);
    System.setProperty("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", socketPath);
    System.setProperty("testcontainers.ryuk.disabled", "true");
    System.setProperty("checks.disable", "true");

    System.out.println("Testcontainers configurato per Rancher Desktop:");
    System.out.println("  DOCKER_HOST=" + dockerHost);
    System.out.println("  Socket=" + socketPath);
  }
}
