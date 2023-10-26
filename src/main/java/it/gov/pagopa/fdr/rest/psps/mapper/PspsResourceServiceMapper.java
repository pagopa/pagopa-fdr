package it.gov.pagopa.fdr.rest.psps.mapper;

import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.rest.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetResponse;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.rest.psps.response.GetAllCreatedResponse;
import it.gov.pagopa.fdr.rest.psps.response.GetAllPublishedResponse;
import it.gov.pagopa.fdr.rest.psps.response.GetCreatedResponse;
import it.gov.pagopa.fdr.service.dto.*;
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

  GetPaymentResponse toGetPaymentResponse(FdrGetPaymentDto fdrGetPaymentDto);

  GetAllResponse toGetAllResponsePublished(FdrAllDto fdrAllDto);

  GetResponse toGetIdResponsePublished(FdrGetDto fdrGetDto);

  GetAllPublishedResponse toGetAllPublishedResponse(FdrAllPublishedDto fdrAllDto);
}
