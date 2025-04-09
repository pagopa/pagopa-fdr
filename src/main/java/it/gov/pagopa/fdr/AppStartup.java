package it.gov.pagopa.fdr;

import io.quarkus.runtime.Startup;
import it.gov.pagopa.fdr.service.ReService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Startup
@ApplicationScoped
public class AppStartup {

  @ConfigProperty(name = "startconfig.enabled")
  boolean startconfig;

  private final Logger log;

  private final Config config;

  private final ReService reService;

  public AppStartup(Logger log, Config config, ReService reService) {
    this.log = log;
    this.config = config;
    this.reService = reService;
  }

  @PostConstruct
  public void init() {
    if (startconfig) {
      log.info("Start Cache ENABLED");
      config.init();
    } else {
      log.info("Start Cache DISABLED");
    }
  }
}
