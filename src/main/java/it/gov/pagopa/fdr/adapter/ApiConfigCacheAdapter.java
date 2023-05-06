//package it.gov.pagopa.fdr.adapter;
//
//import io.smallrye.common.annotation.Blocking;
//import it.gov.pagopa.fdr.Config;
//import jakarta.inject.Inject;
//import jakarta.ws.rs.GET;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import org.jboss.logging.Logger;
//import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
//
//@Produces(MediaType.APPLICATION_JSON)
//@Path("/petstore")
//public class ApiConfigCacheAdapter {
//
//  @Inject Logger log;
//
//  @Inject Config config;
//
//
//  @GET
//  @Blocking
//  public Response testCache() {
//    ConfigDataV1 configData = config.getCache();
//
//    configData.getChannels().entrySet().stream()
//        .forEach(a -> log.infof("key:[%s], value:[%s]", a.getKey(), a.getValue().getChannelCode()));
//
//    return Response.ok().build();
//  }
//}
