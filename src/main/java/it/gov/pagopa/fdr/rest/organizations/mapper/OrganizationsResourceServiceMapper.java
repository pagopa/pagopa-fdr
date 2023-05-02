package it.gov.pagopa.fdr.rest.organizations.mapper;

import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface OrganizationsResourceServiceMapper {

  OrganizationsResourceServiceMapper INSTANCE =
      Mappers.getMapper(OrganizationsResourceServiceMapper.class);

  GetIdResponse toGetIdResponse(ReportingFlowGetDto reportingFlowGetDto);

  GetPaymentResponse toGetPaymentResponse(ReportingFlowGetPaymentDto reportingFlowGetDto);

  GetAllResponse toGetAllResponse(ReportingFlowByIdEcDto reportingFlowByIdEcDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
