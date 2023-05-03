package it.gov.pagopa.fdr.adapter;

import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.api.NodeCacheApi;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;

@Produces(MediaType.APPLICATION_JSON)
@Path("/petstore")
public class ApiConfigCacheAdapter {

  @Inject Logger log;

  @Inject @RestClient NodeCacheApi nodeCacheApi;

//  private final NodeCacheApi nodeCacheApi2;
//
//  public ApiConfigCacheAdapter() {
//    nodeCacheApi2 =
//        RestClientBuilder.newBuilder()
//            .baseUri(URI.create("https://stage.code.quarkus.io/api"))
//            .build(NodeCacheApi.class);
//  }

  @GET
  @Blocking
  public Response testCache() {

    ConfigDataV1 cache = nodeCacheApi.cache();
    cache.getChannels().entrySet().stream()
        .forEach(a -> log.infof("key:[$s], value:[$s]", a.getKey(), a.getValue().getChannelCode()));

    return Response.ok().build();
  }
}
