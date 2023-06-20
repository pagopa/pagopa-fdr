package it.gov.pagopa.fdr.service.re;

import it.gov.pagopa.fdr.service.re.model.ReAbstract;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReService {

  @Inject Logger log;

  public <T extends ReAbstract> void sendEvent(T re) {
    log.info(re.toString());
  }
}
