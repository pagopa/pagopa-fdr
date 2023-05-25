package it.gov.pagopa.fdr;

import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;

public class NoInternalSchemaFilter implements OASFilter {

  @ConfigProperty(name = "operations.filter")
  String filter;

  @Override
  public void filterOpenAPI(OpenAPI openAPI) {
    String profilefilter =
        ConfigProvider.getConfig().getConfigValue("operations.filter").getValue();
    Map<String, PathItem> collect =
        openAPI.getPaths().getPathItems().entrySet().stream()
            .filter(d -> d.getKey().matches(profilefilter))
            .collect(Collectors.toMap(r -> r.getKey(), r -> r.getValue()));
    openAPI.getPaths().setPathItems(collect);
  }
}
