package it.gov.pagopa.fdr;

import io.quarkus.runtime.Startup;
import it.gov.pagopa.fdr.service.conversion.ConversionService;
import it.gov.pagopa.fdr.service.flowTx.FlowTxService;
import it.gov.pagopa.fdr.service.history.HistoryService;
import it.gov.pagopa.fdr.service.re.ReService;
import it.gov.pagopa.fdr.service.reportedIuv.ReportedIuvService;
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

  @ConfigProperty(name = "eHub.reportediuv.enabled")
  boolean eHubReportedIuvEnabled;

  @ConfigProperty(name = "eHub.flowtx.enabled")
  boolean eHubFlowTxEnabled;

  private final Logger log;

  private final Config config;

  private final ConversionService conversionQueue;

  private final ReService reService;
  private final HistoryService historyService;

  private final ReportedIuvService reportedIuvService;
  private final FlowTxService flowTxService;

  public AppStartup(
      Logger log,
      Config config,
      ConversionService conversionQueue,
      ReService reService,
      HistoryService historyService,
      ReportedIuvService reportedIuvService,
      FlowTxService flowTxService) {
    this.log = log;
    this.config = config;
    this.conversionQueue = conversionQueue;
    this.reService = reService;
    this.historyService = historyService;
    this.reportedIuvService = reportedIuvService;
    this.flowTxService = flowTxService;
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

    if (eHubReportedIuvEnabled) {
      log.info("Start EventHub ReportedIUV");
      reportedIuvService.init();
    } else {
      log.info("Start EventHub ReportedIUV");
    }

    if (eHubFlowTxEnabled) {
      log.info("Start EventHub FlowTx");
      flowTxService.init();
    } else {
      log.info("Start EventHub FlowTx");
    }
  }
}
