package it.gov.pagopa.fdr.rest.reportingFlow.mapper;

import it.gov.pagopa.fdr.rest.reportingFlow.model.Metadata;
import it.gov.pagopa.fdr.rest.reportingFlow.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.reportingFlow.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.CreateRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.reportingFlow.response.GetPaymentResponse;
import it.gov.pagopa.fdr.service.reportingFlow.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.MetadataDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowStatusEnumDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ReportingFlowDtoServiceMapper {

  ReportingFlowDtoServiceMapper INSTANCE = Mappers.getMapper(ReportingFlowDtoServiceMapper.class);

  ReportingFlowDto toReportingFlowDto(CreateRequest createRequest);

  GetIdResponse toGetIdResponse(ReportingFlowGetDto reportingFlowGetDto);

  GetPaymentResponse toGetPaymentResponse(ReportingFlowGetPaymentDto reportingFlowGetDto);

  AddPaymentDto toAddPaymentDto(AddPaymentRequest addPaymentRequest);

  DeletePaymentDto toDeletePaymentDto(DeletePaymentRequest deletePaymentRequest);

  ReportingFlowStatusEnum toReportingFlowStatusEnum(
      ReportingFlowStatusEnumDto reportingFlowStatusEnumDto);

  GetAllResponse toGetAllResponse(ReportingFlowByIdEcDto reportingFlowByIdEcDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
