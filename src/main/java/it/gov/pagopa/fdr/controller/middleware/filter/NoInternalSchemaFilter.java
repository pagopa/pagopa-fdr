package it.gov.pagopa.fdr.controller.middleware.filter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;

public class NoInternalSchemaFilter implements OASFilter {

  @Override
  public void filterOpenAPI(OpenAPI openAPI) {
    String profilefilter =
        ConfigProvider.getConfig().getConfigValue("operations.filter").getValue();
    Map<String, PathItem> collect =
        openAPI.getPaths().getPathItems().entrySet().stream()
            .filter(d -> d.getKey().matches(profilefilter))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    openAPI.getPaths().setPathItems(collect);
  }
}
