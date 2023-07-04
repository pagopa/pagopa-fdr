package it.gov.pagopa.fdr;

import io.quarkus.runtime.Startup;
import it.gov.pagopa.fdr.service.conversion.ConversionService;
import it.gov.pagopa.fdr.service.re.ReService;
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

  @ConfigProperty(name = "queue.conversion.enabled")
  boolean queueConversionEnabled;

  @ConfigProperty(name = "eHub.re.enabled")
  boolean eHubReEnabled;

  @Inject Logger log;

  @Inject Config config;

  @Inject ConversionService conversionQueue;

  @Inject ReService reService;

  @PostConstruct
  public void init() {
    if (startconfig) {
      log.info("Start Cache ENABLED");
      config.init();
    } else {
      log.info("Start Cache DISABLED");
    }

    if (queueConversionEnabled) {
      log.info("Start Queue Conversion ENABLED");
      conversionQueue.init();
    } else {
      log.info("Start Queue Conversion DISABLED");
    }

    if (eHubReEnabled) {
      log.info("Start EventHub Re and blob ENABLED");
      reService.init();
    } else {
      log.info("Start EventHub Re and blob DISABLED");
    }
  }
}
