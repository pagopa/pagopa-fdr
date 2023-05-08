package it.gov.pagopa.fdr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.api.NodeCacheApi;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@Startup
@ApplicationScoped
public class Config {

  @PostConstruct
  public void init() {
    ConfigDataV1 newCache = nodeCacheApi.cache();
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
  @Inject @RestClient NodeCacheApi nodeCacheApi;

  @Scheduled(cron = "{api_config_cache.cron.expr}")
  void cronJobApiconfigCache(ScheduledExecution execution) {
    log.infof("Schedule api-config-cache %s", execution.getScheduledFireTime());
    String version = cache.getVersion();
    String newVersion = nodeCacheApi.idV1().getVersion();
    if (version.equals(newVersion)) {
      log.debugf("CACHE NOT UPDATED. Version [%s]", cache.getVersion());
    } else {
      log.debugf("CACHE UPDATED. Version  [%s] -> [%s]", version, newVersion);
      this.cache = nodeCacheApi.cache();
    }
  }
}
