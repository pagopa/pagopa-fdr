package it.gov.pagopa.fdr.rest.psps.mapper;

import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.rest.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.response.GetAllCreatedResponse;
import it.gov.pagopa.fdr.rest.psps.response.GetCreatedResponse;
import it.gov.pagopa.fdr.service.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.dto.FdrAllCreatedDto;
import it.gov.pagopa.fdr.service.dto.FdrDto;
import it.gov.pagopa.fdr.service.dto.FdrGetCreatedDto;
import it.gov.pagopa.fdr.service.dto.FdrStatusEnumDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PspsResourceServiceMapper {

  PspsResourceServiceMapper INSTANCE = Mappers.getMapper(PspsResourceServiceMapper.class);

  FdrDto toReportingFlowDto(CreateRequest createRequest);

  AddPaymentDto toAddPaymentDto(AddPaymentRequest addPaymentRequest);

  DeletePaymentDto toDeletePaymentDto(DeletePaymentRequest deletePaymentRequest);

  GetAllCreatedResponse toGetAllResponse(FdrAllCreatedDto fdrAllDto);

  ReportingFlowStatusEnum toReportingFlowStatusEnum(FdrStatusEnumDto fdrStatusEnumDto);

  Metadata toMetadata(MetadataDto metadataDto);

  GetCreatedResponse toGetCreatedResponse(FdrGetCreatedDto fdrAllDto);
}
