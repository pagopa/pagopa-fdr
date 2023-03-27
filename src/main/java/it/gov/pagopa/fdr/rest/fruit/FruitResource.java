package it.gov.pagopa.fdr.rest.fruit;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.fruit.mapper.FruitRestServiceMapper;
import it.gov.pagopa.fdr.rest.fruit.request.FruitAddRequest;
import it.gov.pagopa.fdr.rest.fruit.request.FruitDeleteRequest;
import it.gov.pagopa.fdr.rest.fruit.response.FruitResponse;
import it.gov.pagopa.fdr.service.fruit.FruitService;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/fruits")
@Tag(name = "Fruit", description = "Fruit operations")
public class FruitResource {

  @Inject Logger log;

  @Inject FruitRestServiceMapper mapper;

  @Inject FruitService fruitService;

  @Operation(summary = "Get list of fruit")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/BadRequest"),
        @APIResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FruitResponse.class)))
      })
  @GET
  public List<FruitResponse> list() {
    log.info("get all fruits");
    return mapper.toListFruitResponse(fruitService.list());
  }

  @Path("/{name}")
  @GET
  @WithSpan(kind = SERVER)
  public FruitResponse get(@SpanAttribute(value = "name") String name) {
    log.infof("get fruit %s", name);
    fruitService.validateGet(name);
    return mapper.toFruitResponse(fruitService.findFruit(name));
  }

  @POST
  public List<FruitResponse> add(FruitAddRequest fruitAddRequest) {
    log.infof("add fruit %s", fruitAddRequest.getName());
    fruitService.validateAdd(fruitAddRequest);
    return mapper.toListFruitResponse(fruitService.add(mapper.toFruitDto(fruitAddRequest)));
  }

  @DELETE
  public List<FruitResponse> delete(FruitDeleteRequest fruitDeleteRequest) {
    log.infof("delete fruit %s", fruitDeleteRequest.getName());
    fruitService.validateDelete(fruitDeleteRequest);
    return mapper.toListFruitResponse(fruitService.delete(fruitDeleteRequest.getName()));
  }
}
