package it.gov.pagopa.fdr.service.fruit;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.repository.entity.Fruit;
import it.gov.pagopa.fdr.rest.fruit.request.FruitAddRequest;
import it.gov.pagopa.fdr.rest.fruit.request.FruitDeleteRequest;
import it.gov.pagopa.fdr.service.fruit.dto.FruitDto;
import it.gov.pagopa.fdr.service.fruit.mapper.FruitEntityServiceMapper;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FruitService {

  @Inject FruitEntityServiceMapper mapper;

  @Inject Logger log;

  public List<FruitDto> list() {
    return mapper.toFruitDtoList(Fruit.listAll());
  }

  @WithSpan(kind = SERVER)
  public void validateGet(String name) {
    if ("fake".equals(name))
      throw new IllegalStateException("Forcing error that handle successfully");
    else if ("fake2".equals(name))
      throw new AppException(AppErrorCodeMessageEnum.FRUIT_BAD_REQUEST, "fake2");
  }

  @WithSpan(kind = SERVER)
  public void validateAdd(@Valid FruitAddRequest fruitAddRequest) {
    log.infof("Validate fruit [%s]", fruitAddRequest.getName());
  }

  @WithSpan(kind = SERVER)
  public void validateDelete(FruitDeleteRequest fruitDeleteRequest) {}

  @WithSpan(kind = SERVER)
  public FruitDto findFruit(@SpanAttribute(value = "name") String name) {
    return mapper.toFruitDto(
        Fruit.findByNameOptional(name)
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.FRUIT_NOT_FOUND, name)));
  }

  @Transactional
  public List<FruitDto> add(FruitDto fruitDto) {
    mapper.toFruit(fruitDto).persist();
    return mapper.toFruitDtoList(Fruit.listAll());
  }

  @Transactional
  public List<FruitDto> delete(String name) {
    Fruit.deleteByName(name);
    return mapper.toFruitDtoList(Fruit.listAll());
  }
}
