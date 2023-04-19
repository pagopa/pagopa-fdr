package it.gov.pagopa.fdr.rest.reportingFlow.mapper;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Metadata;
import it.gov.pagopa.fdr.rest.reportingFlow.model.ReportingFlow;
import it.gov.pagopa.fdr.rest.reportingFlow.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetAllResponse;
import it.gov.pagopa.fdr.service.reportingFlow.dto.MetadataDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowStatusEnumDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ReportingFlowDtoServiceMapper {

  ReportingFlowDtoServiceMapper INSTANCE = Mappers.getMapper(ReportingFlowDtoServiceMapper.class);

  ReportingFlowDto toReportingFlowDto(CreateRequest createRequest);

  ReportingFlow toReportingFlow(ReportingFlowGetDto reportingFlowGetDto);

  ReportingFlowStatusEnum toReportingFlowStatusEnum(
      ReportingFlowStatusEnumDto reportingFlowStatusEnumDto);

  GetAllResponse toGetAllResponse(ReportingFlowByIdEcDto reportingFlowByIdEcDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
