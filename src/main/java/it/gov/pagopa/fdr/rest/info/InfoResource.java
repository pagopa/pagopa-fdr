package it.gov.pagopa.fdr.rest.info;

import it.gov.pagopa.fdr.rest.info.response.Info;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@Path("/info")
public class InfoResource {

  @Inject Logger log;

  @ConfigProperty(name = "quarkus.application.name", defaultValue = "FDR")
  String name;

  @ConfigProperty(name = "quarkus.application.version", defaultValue = "0.0.0")
  String version;

  @ConfigProperty(name = "app.environment", defaultValue = "dev")
  String environment;

  @GET
  public Info hello() {
    MDC.put("terra", "prova");
    log.info("info");

    return Info.builder().name(name).version(version).environment(environment).build();
  }
}
