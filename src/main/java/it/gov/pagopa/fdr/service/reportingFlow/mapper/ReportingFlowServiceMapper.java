package it.gov.pagopa.fdr.service.reportingFlow.mapper;

import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlow;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ReportingFlowServiceMapper {

  ReportingFlowServiceMapper INSTANCE = Mappers.getMapper(ReportingFlowServiceMapper.class);

  ReportingFlow toReportingFlow(ReportingFlowDto reportingFlowDto);

  ReportingFlowGetDto toReportingFlowGetDto(ReportingFlow reportingFlow);

  default String toGetResponse(ObjectId reportingFlowGetDto) {
    return reportingFlowGetDto.toString();
  }
}
