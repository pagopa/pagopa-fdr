package it.gov.pagopa.fdr.rest.organizations.mapper;

import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllInternalResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetResponse;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowInternalDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface OrganizationsResourceServiceMapper {

  OrganizationsResourceServiceMapper INSTANCE =
      Mappers.getMapper(OrganizationsResourceServiceMapper.class);

  GetResponse toGetIdResponse(ReportingFlowGetDto reportingFlowGetDto);

  GetPaymentResponse toGetPaymentResponse(ReportingFlowGetPaymentDto reportingFlowGetDto);

  GetAllResponse toGetAllResponse(ReportingFlowByIdEcDto reportingFlowByIdEcDto);

  GetAllInternalResponse toGetAllInternalResponse(ReportingFlowInternalDto reportingFlowByIdEcDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
