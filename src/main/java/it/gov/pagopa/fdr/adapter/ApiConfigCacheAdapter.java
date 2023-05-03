package it.gov.pagopa.fdr.adapter;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Produces(MediaType.APPLICATION_JSON)
@Path("/petstore")
public class ApiConfigCacheAdapter {

  @Inject Logger log;

  //  private final NodeCacheApi nodeCacheApi;
  //
  //  public ApiConfigCacheAdapter() {
  //    nodeCacheApi = RestClientBuilder.newBuilder()
  //        .baseUri(URI.create("https://stage.code.quarkus.io/api"))
  //        .build(NodeCacheApi.class);
  //  }

  @GET
  public Response testCache() {

    //    ConfigDataV1 cache = nodeCacheApi.cache();
    //    cache.getChannels().entrySet().stream().forEach(a -> log.infof("key:[$s],
    // value:[$s]",a.getKey(),a.getValue().getChannelCode()));
    return Response.ok().build();
  }
}
