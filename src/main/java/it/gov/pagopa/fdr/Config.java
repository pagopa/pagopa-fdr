package it.gov.pagopa.fdr;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.api.NodeCacheApi;
import org.openapi.quarkus.api_config_cache_json.model.CacheVersion;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@ApplicationScoped
public class Config {

  @PostConstruct
  public void init() {
    cache = nodeCacheApi.cache();
    log.infof("CACHE INIT. Version [%s]", cache.getVersion());
    log.info(cache.hashCode());
  }

  private ConfigDataV1 cache = null;

  @Inject Logger log;
  @Inject @RestClient NodeCacheApi nodeCacheApi;

  public ConfigDataV1 get() {
    return cache;
  }

  @Scheduled(cron = "{api_config_cache.cron.expr}")
  void cronJobWithExpressionInConfig(ScheduledExecution execution) {
    log.info(execution.getScheduledFireTime());
    if (cache == null) {
      throw new RuntimeException();
    }

    CacheVersion cacheVersion = nodeCacheApi.idV1();
    if (!cacheVersion.getVersion().equals(cache.getVersion())) {
      String oldVersion = cache.getVersion();
      cache = nodeCacheApi.cache();
      log.infof("CACHE UPDATED. Version  [%s] -> [%s]", oldVersion, cache.getVersion());
      log.info(cache.hashCode());
    } else {
      log.infof("CACHE NOT UPDATED. Version [%s]", cache.getVersion());
      log.info(cache.hashCode());
    }
  }
}
