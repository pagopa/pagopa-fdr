package it.gov.pagopa.fdr.rest.reportingFlow.mapper;

import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateRequest;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ReportingFlowDtoServiceMapper {

  ReportingFlowDtoServiceMapper INSTANCE = Mappers.getMapper(ReportingFlowDtoServiceMapper.class);

  ReportingFlowDto toReportingFlowDto(CreateRequest createRequest);
}
