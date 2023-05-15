package it.gov.pagopa.fdr;

import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URISyntaxException;
import org.jboss.logging.Logger;

@Startup
@ApplicationScoped
@UnlessBuildProfile("test")
public class AppStartup {

  @Inject Logger log;

  @Inject Config config;

  @PostConstruct
  public void init() throws URISyntaxException {
    config.init();
  }
}
