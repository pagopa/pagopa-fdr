package it.gov.pagopa.fdr.service.upload.mapper;

import it.gov.pagopa.fdr.repository.entity.flow.Flow;
import it.gov.pagopa.fdr.repository.entity.flow.Flow.FlowFile;
import it.gov.pagopa.fdr.service.upload.dto.FlowDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface FlowEntityServiceMapper {

  FlowEntityServiceMapper INSTANCE = Mappers.getMapper(FlowEntityServiceMapper.class);

  FlowDto toFlowDto(Flow flow);

  Flow toFlow(FlowDto flowDto);

  FlowFile toFlowFile(FlowDto flowDto);
}
