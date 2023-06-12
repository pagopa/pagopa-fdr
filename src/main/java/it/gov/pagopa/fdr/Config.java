package it.gov.pagopa.fdr;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.net.URI;
import java.util.Collections;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.api.FdrCacheApi;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class Config {

  @ConfigProperty(name = "adapter.api_config_cache.url")
  String url;

  @ConfigProperty(name = "adapter.api_config_cache.api-key-name")
  String apiKeyName;

  @ConfigProperty(name = "adapter.api_config_cache.api-key-value")
  String apiKeyValue;

  private FdrCacheApi nodeCacheApi;

  //  @PostConstruct
  @SneakyThrows
  public void init() {
    nodeCacheApi =
        RestClientBuilder.newBuilder()
            .baseUri(new URI(url))
            .register(
                (ClientRequestFilter)
                    context ->
                        context
                            .getHeaders()
                            .put(apiKeyName, Collections.singletonList(apiKeyValue)))
            .build(FdrCacheApi.class);

    ConfigDataV1 newCache = nodeCacheApi.cache(null);
    log.debugf("Cache init. Version [%s]", newCache.getVersion());
    this.cache = newCache;
  }

  @Inject ObjectMapper objectMapper;

  ConfigDataV1 cache;

  @SneakyThrows
  public ConfigDataV1 getClonedCache() {
    return objectMapper.readValue(objectMapper.writeValueAsString(this.cache), ConfigDataV1.class);
  }

  @Inject Logger log;

  @Scheduled(cron = "{api_config_cache.cron.expr}")
  void cronJobApiconfigCache(ScheduledExecution execution) {
    log.debugf("Schedule api-config-cache %s", execution.getScheduledFireTime());
    String version = cache.getVersion();
    String newVersion = nodeCacheApi.idV1().getVersion();
    if (version.equals(newVersion)) {
      log.debugf("Cache NOT updated. Version [%s]", cache.getVersion());
    } else {
      log.debugf("Cache updated. Version  [%s] -> [%s]", version, newVersion);
      this.cache = nodeCacheApi.cache(null);
    }
  }
}
