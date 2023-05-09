package it.gov.pagopa.fdr.rest.internal.mapper;

import it.gov.pagopa.fdr.rest.internal.response.InternalsGetAllResponse;
import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface InternalsResourceServiceMapper {

  InternalsResourceServiceMapper INSTANCE = Mappers.getMapper(InternalsResourceServiceMapper.class);

  InternalsGetAllResponse toGetAllResponse(ReportingFlowByIdEcDto reportingFlowByIdEcDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
