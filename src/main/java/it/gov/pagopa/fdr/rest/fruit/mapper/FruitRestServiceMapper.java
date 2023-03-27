package it.gov.pagopa.fdr.rest.fruit.mapper;

import it.gov.pagopa.fdr.rest.fruit.request.FruitAddRequest;
import it.gov.pagopa.fdr.rest.fruit.response.FruitResponse;
import it.gov.pagopa.fdr.service.fruit.dto.FruitDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface FruitRestServiceMapper {

  FruitRestServiceMapper INSTANCE = Mappers.getMapper(FruitRestServiceMapper.class);

  FruitDto toFruitDto(FruitAddRequest fruitRequest);

  FruitResponse toFruitResponse(FruitDto fruitDto);

  List<FruitResponse> toListFruitResponse(List<FruitDto> fruitDto);
}
