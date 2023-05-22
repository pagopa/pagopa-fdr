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

  @ConfigProperty(name = "quarkus.profile")
  String profile;

  @Inject Logger log;

  @Inject Config config;

  @PostConstruct
  public void init() {
    if ("openapi".equals(profile)) {
      log.info("NOT START CONFIG CLASS");
    } else {
      log.info("START CONFIG CLASS");
      config.init();
    }
  }
}
