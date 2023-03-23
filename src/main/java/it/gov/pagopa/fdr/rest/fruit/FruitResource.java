package it.gov.pagopa.fdr.rest.fruit;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.fruit.mapper.FruitRestServiceMapper;
import it.gov.pagopa.fdr.rest.fruit.request.FruitAddRequest;
import it.gov.pagopa.fdr.rest.fruit.request.FruitDeleteRequest;
import it.gov.pagopa.fdr.service.FruitService;
import it.gov.pagopa.fdr.service.dto.FruitDto;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.jboss.logging.Logger;

@Path("/fruits")
public class FruitResource {

  @Inject Logger log;

  @Inject FruitRestServiceMapper mapper;

  @Inject FruitService fruitService;

  @GET
  public List<FruitDto> list() {
    log.info("get all fruits");
    return fruitService.list();
  }

  @Path("/{name}")
  @GET
  @WithSpan(kind = SERVER)
  public FruitDto get(@SpanAttribute(value = "name") String name) {
    log.infof("get fruit %s", name);
    fruitService.validateGet(name);
    return fruitService.findFruit(name);
  }

  @POST
  public List<FruitDto> add(FruitAddRequest fruitAddRequest) {
    log.infof("add fruit %s", fruitAddRequest.getName());
    fruitService.validateAdd(fruitAddRequest);
    return fruitService.add(mapper.toFruitDto(fruitAddRequest));
  }

  @DELETE
  public List<FruitDto> delete(FruitDeleteRequest fruitDeleteRequest) {
    log.infof("delete fruit %s", fruitDeleteRequest.getName());
    fruitService.validateDelete(fruitDeleteRequest);
    return fruitService.delete(fruitDeleteRequest.getName());
  }
}
