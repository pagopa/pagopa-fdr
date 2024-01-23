package it.gov.pagopa.fdr;

import io.quarkus.runtime.Startup;
import it.gov.pagopa.fdr.service.conversion.ConversionService;
import it.gov.pagopa.fdr.service.history.HistoryService;
import it.gov.pagopa.fdr.service.re.ReService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
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

  @ConfigProperty(name = "history.enabled")
  boolean historyEnabled;

  private final Logger log;

  private final Config config;

  private final ConversionService conversionQueue;

  private final ReService reService;
  private final HistoryService historyService;

  public AppStartup(
      Logger log,
      Config config,
      ConversionService conversionQueue,
      ReService reService,
      HistoryService historyService) {
    this.log = log;
    this.config = config;
    this.conversionQueue = conversionQueue;
    this.reService = reService;
    this.historyService = historyService;
  }

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

    if (historyEnabled) {
      log.info("History ENABLED");
      historyService.init();
    } else {
      log.info("History DISABLED");
    }
  }
}
