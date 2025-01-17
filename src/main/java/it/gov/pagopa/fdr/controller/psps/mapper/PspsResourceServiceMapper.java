package it.gov.pagopa.fdr.controller.psps.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.enums.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.request.AddPaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PspsResourceServiceMapper {

  PspsResourceServiceMapper INSTANCE = Mappers.getMapper(PspsResourceServiceMapper.class);

  FdrDto toReportingFlowDto(CreateFlowRequest createRequest);

  AddPaymentDto toAddPaymentDto(AddPaymentRequest addPaymentRequest);

  DeletePaymentDto toDeletePaymentDto(DeletePaymentRequest deletePaymentRequest);

  PaginatedFlowsCreatedResponse toGetAllResponse(FdrAllCreatedDto fdrAllDto);

  ReportingFlowStatusEnum toReportingFlowStatusEnum(FdrStatusEnumDto fdrStatusEnumDto);

  Metadata toMetadata(MetadataDto metadataDto);

  SingleFlowCreatedResponse toGetCreatedResponse(FdrGetCreatedDto fdrAllDto);

  PaginatedPaymentsResponse toGetPaymentResponse(FdrGetPaymentDto fdrGetPaymentDto);

  PaginatedFlowsResponse toGetAllResponsePublished(FdrAllDto fdrAllDto);

  SingleFlowResponse toGetIdResponsePublished(FdrGetDto fdrGetDto);

  PaginatedFlowsPublishedResponse toGetAllPublishedResponse(FdrAllPublishedDto fdrAllDto);
}
