package it.gov.pagopa.fdr.test.config;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.io.File;

/**
 * Listener JUnit che configura Testcontainers prima di qualsiasi test.
 * Viene caricato automaticamente tramite ServiceLoader da META-INF/services.
 * Imposta le property Rancher Desktop solo se il socket esiste (locale),
 * in modo da non interferire in CI (GitHub Actions).
 */
public class TestcontainersConfigurationListener implements LauncherSessionListener {

  private static final String RANCHER_SOCKET_PATH =
      System.getProperty("user.home") + "/.rd/docker.sock";

  @Override
  public void launcherSessionOpened(LauncherSession session) {
    // Ryuk disabilitato sempre (sia in locale che in CI)
    System.setProperty("testcontainers.ryuk.disabled", "true");
    System.setProperty("checks.disable", "true");

    // Configurazione Rancher Desktop: attiva solo se il socket esiste (ambiente locale)
    if (new File(RANCHER_SOCKET_PATH).exists()) {
      String dockerHost = "unix://" + RANCHER_SOCKET_PATH;
      System.setProperty("DOCKER_HOST", dockerHost);
      System.setProperty("docker.host", dockerHost);
      System.setProperty("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", RANCHER_SOCKET_PATH);
      System.out.println("[Testcontainers] Rancher Desktop rilevato: DOCKER_HOST=" + dockerHost);
    } else {
      System.out.println("[Testcontainers] Rancher Desktop non rilevato, uso configurazione Docker di default (CI)");
    }
  }
}
