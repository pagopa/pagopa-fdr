package it.gov.pagopa.fdr.rest.fruit.mapper;

import it.gov.pagopa.fdr.rest.fruit.request.FruitAddRequest;
import it.gov.pagopa.fdr.service.dto.FruitDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface FruitRestServiceMapper {

  FruitRestServiceMapper INSTANCE = Mappers.getMapper(FruitRestServiceMapper.class);

  FruitDto toFruitDto(FruitAddRequest fruitRequest);
}
