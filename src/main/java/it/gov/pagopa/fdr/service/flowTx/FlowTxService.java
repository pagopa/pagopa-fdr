package it.gov.pagopa.fdr.service.flowTx;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.fdr.service.flowTx.model.FlowTx;
import it.gov.pagopa.fdr.util.EventHub;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FlowTxService {

  private final Logger log;

  @ConfigProperty(name = "ehub.flowtx.connect-str")
  String eHubConnectStr;

  @ConfigProperty(name = "ehub.flowtx.name")
  String eHubName;

  private final ObjectMapper objectMapper;
  private EventHub eventHub;

  public FlowTxService(Logger log, ObjectMapper objectMapper) {
    this.log = log;
    this.objectMapper = objectMapper;
  }

  public void init() {
    log.infof("EventHub flowtx init. EventHub name [%s]", eHubName);

    this.eventHub = new EventHub(this.log, this.objectMapper, eHubConnectStr, eHubName);
  }

  public final void sendEvent(FlowTx... list) {
    if (this.eventHub == null) {
      log.debugf("EventHub [%s] NOT INITIALIZED", eHubName);
    } else {
      if (list != null) {
        eventHub.sendEvent(Arrays.stream(list).toList());
      } else {
        log.debug("list is null");
      }
    }
  }
}
