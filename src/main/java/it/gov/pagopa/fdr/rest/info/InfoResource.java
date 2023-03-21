package it.gov.pagopa.fdr.rest.info;

import it.gov.pagopa.fdr.rest.info.response.Info;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Path("/info")
public class InfoResource {

  @Inject Logger log;

  @ConfigProperty(name = "app.name", defaultValue = "app")
  String name;

  @ConfigProperty(name = "app.version", defaultValue = "0.0.0")
  String version;

  @ConfigProperty(name = "app.environment", defaultValue = "local")
  String environment;

  @GET
  public Info hello() {
    log.infof("Info environment: [%s] - name: [%s] - version: [%s]", environment, name, version);

    return Info.builder()
        .name(name)
        .version(version)
        .environment(environment)
        .description(AppMessageUtil.getMessage("app.description"))
        .build();
  }
}
