package it.gov.pagopa.fdr.service.reportedIuv;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.service.reportedIuv.model.ReportedIuv;
import it.gov.pagopa.fdr.util.EventHub;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportedIuvService {

  private final Logger log;

  @ConfigProperty(name = "ehub.reportediuv.connect-str")
  String eHubConnectStr;

  @ConfigProperty(name = "ehub.reportediuv.name")
  String eHubName;

  private final ObjectMapper objectMapper;

  private EventHub eventHub;

  public ReportedIuvService(Logger log, ObjectMapper objectMapper) {
    this.log = log;
    this.objectMapper = objectMapper;
  }

  public void init() {
    log.infof("EventHub reportediuv init. EventHub name [%s]", eHubName);
    this.eventHub = new EventHub(this.log, this.objectMapper, eHubConnectStr, eHubName);
  }

  public final void sendEvent(List<ReportedIuv> list) {
    if (this.eventHub == null) {
      log.debugf("EventHub [%s] NOT INITIALIZED", eHubName);
    } else {
      eventHub.sendEvent(list);
    }
  }
}
