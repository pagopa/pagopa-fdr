package it.gov.pagopa.fdr.controller.organizations.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.service.dto.FdrAllDto;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.FdrGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface OrganizationsResourceServiceMapper {

  OrganizationsResourceServiceMapper INSTANCE =
      Mappers.getMapper(OrganizationsResourceServiceMapper.class);

  SingleFlowResponse toGetIdResponse(FdrGetDto fdrGetDto);

  PaginatedPaymentsResponse toGetPaymentResponse(FdrGetPaymentDto fdrGetPaymentDto);

  PaginatedFlowsResponse toGetAllResponse(FdrAllDto fdrAllDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
