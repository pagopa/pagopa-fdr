package it.gov.pagopa.fdr.service;


import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.service.mapper.FruitEntityServiceMapper;
import it.gov.pagopa.fdr.repository.entity.Fruit;
import it.gov.pagopa.fdr.service.dto.FruitDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

@ApplicationScoped
public class FruitService {

    @Inject
    FruitEntityServiceMapper mapper;

    public List<FruitDto> list(){
        return mapper.toFruitDtoList(Fruit.listAll());
    }

    @WithSpan(kind = SERVER)
    public FruitDto findFruit(@SpanAttribute(value = "name") String name) {
        return mapper.toFruitDto(
                Fruit.findByNameOptional(name)
                        .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.FRUIT_NOT_FOUND,name)));
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