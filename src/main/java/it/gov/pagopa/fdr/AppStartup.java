package it.gov.pagopa.fdr;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Startup
@ApplicationScoped
public class AppStartup {

  @ConfigProperty(name = "startconfig.enabled")
  boolean startconfig;

  @Inject Logger log;

  @Inject Config config;

  @PostConstruct
  public void init() {
    if (startconfig) {
      log.info("START CONFIG CLASS");
      config.init();
    } else {
      log.info("NOT START CONFIG CLASS");
    }
  }
}
