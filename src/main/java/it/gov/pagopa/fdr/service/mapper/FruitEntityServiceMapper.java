package it.gov.pagopa.fdr.service.mapper;

import it.gov.pagopa.fdr.repository.entity.Fruit;
import it.gov.pagopa.fdr.service.dto.FruitDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface FruitEntityServiceMapper {

    FruitEntityServiceMapper INSTANCE = Mappers.getMapper(FruitEntityServiceMapper.class);

    FruitDto toFruitDto(Fruit fruit);
    Fruit toFruit(FruitDto fruit);

    List<FruitDto> toFruitDtoList(List<Fruit> fruit);
}
