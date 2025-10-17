package it.gov.pagopa.fdr;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Timed;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
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

  public Config(ObjectMapper objectMapper, Logger log) {
    this.objectMapper = objectMapper;
    this.log = log;
  }

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

  private final ObjectMapper objectMapper;

  ConfigDataV1 cache;

  @SneakyThrows
  @Timed(value = "config.cache.task", description = "Time taken to perform get cloned cache")
  public ConfigDataV1 getClonedCache() {
    if (this.cache == null) {
      log.debug("Api config cache NOT INITIALIZED. Initializing it by demand.");
      this.cache = nodeCacheApi.cache(null);
    }
    //return objectMapper.readValue(objectMapper.writeValueAsString(this.cache), ConfigDataV1.class);
    return this.cache;
  }

  private final Logger log;

  @Scheduled(cron = "{api_config_cache.cron.expr}")
  void cronJobApiconfigCache(ScheduledExecution execution) {
    if (this.cache == null) {
      log.debug("Api config cache NOT INITIALIZED");
    } else {
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
}
