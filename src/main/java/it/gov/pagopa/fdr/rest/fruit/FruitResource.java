package it.gov.pagopa.fdr.rest.fruit;


import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.rest.fruit.mapper.FruitRestServiceMapper;
import it.gov.pagopa.fdr.rest.fruit.request.FruitAddRequest;
import it.gov.pagopa.fdr.rest.fruit.request.FruitDeleteRequest;
import it.gov.pagopa.fdr.service.dto.FruitDto;
import it.gov.pagopa.fdr.service.FruitService;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

@Path("/fruits")
public class FruitResource {

    @Inject
    Logger log;

    @Inject
    FruitRestServiceMapper mapper;

    @Inject
    FruitService fruitService;


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
        if("fake".equals(name))
            throw new IllegalStateException("Forcing error that handle successfully");
        else if("fake2".equals(name))
            throw new AppException(AppErrorCodeMessageEnum.FRUIT_BAD_REQUEST, "fake2");
        return fruitService.findFruit(name);
    }

    @POST
    public List<FruitDto> add(@Valid FruitAddRequest fruitAddRequest) {
        log.infof("add fruit %s", fruitAddRequest.getName());
        return fruitService.add(mapper.toFruitDto(fruitAddRequest));
    }

    @DELETE
    public List<FruitDto> delete(FruitDeleteRequest fruitDeleteRequest) {
        log.infof("delete fruit %s", fruitDeleteRequest.getName());
        return fruitService.delete(fruitDeleteRequest.getName());
    }
}
