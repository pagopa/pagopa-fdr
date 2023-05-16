package it.gov.pagopa.fdr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.api.FdrCacheApi;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

// @Startup
@ApplicationScoped
// @UnlessBuildProfile("test")
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
  public void init(){
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
    log.debugf("CACHE INIT. Version [%s]", newCache.getVersion());
    this.cache = newCache;
  }

  @Inject ObjectMapper objectMapper;

  ConfigDataV1 cache;

  public ConfigDataV1 getClonedCache() {
    try {
      ConfigDataV1 deepCopy =
          objectMapper.readValue(objectMapper.writeValueAsString(this.cache), ConfigDataV1.class);
      return deepCopy;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Inject Logger log;
  //  @Inject @RestClient
  //  NodeCacheApi nodeCacheApi;

  @Scheduled(cron = "{api_config_cache.cron.expr}")
  void cronJobApiconfigCache(ScheduledExecution execution) {
    log.debugf("Schedule api-config-cache %s", execution.getScheduledFireTime());
    String version = cache.getVersion();
    String newVersion = nodeCacheApi.idV1().getVersion();
    if (version.equals(newVersion)) {
      log.debugf("CACHE NOT UPDATED. Version [%s]", cache.getVersion());
    } else {
      log.debugf("CACHE UPDATED. Version  [%s] -> [%s]", version, newVersion);
      this.cache = nodeCacheApi.cache(null);
    }
  }
}
