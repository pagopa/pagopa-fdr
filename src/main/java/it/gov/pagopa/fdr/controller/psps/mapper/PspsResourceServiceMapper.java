package it.gov.pagopa.fdr.controller.psps.mapper;

import it.gov.pagopa.fdr.controller.model.Metadata;
import it.gov.pagopa.fdr.controller.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.controller.model.flow.FlowResponse;
import it.gov.pagopa.fdr.controller.model.flow.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.payment.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.controller.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.psps.request.CreateRequest;
import it.gov.pagopa.fdr.controller.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.psps.response.GetAllCreatedResponse;
import it.gov.pagopa.fdr.controller.psps.response.GetAllPublishedResponse;
import it.gov.pagopa.fdr.controller.psps.response.GetCreatedResponse;
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

  PaginatedPaymentsResponse toGetPaymentResponse(FdrGetPaymentDto fdrGetPaymentDto);

  PaginatedFlowsResponse toGetAllResponsePublished(FdrAllDto fdrAllDto);

  FlowResponse toGetIdResponsePublished(FdrGetDto fdrGetDto);

  GetAllPublishedResponse toGetAllPublishedResponse(FdrAllPublishedDto fdrAllDto);
}
