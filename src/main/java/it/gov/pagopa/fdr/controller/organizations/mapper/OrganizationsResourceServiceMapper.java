package it.gov.pagopa.fdr.controller.organizations.mapper;

import it.gov.pagopa.fdr.controller.model.Metadata;
import it.gov.pagopa.fdr.controller.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.controller.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.controller.organizations.response.GetResponse;
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

  GetResponse toGetIdResponse(FdrGetDto fdrGetDto);

  GetPaymentResponse toGetPaymentResponse(FdrGetPaymentDto fdrGetPaymentDto);

  GetAllResponse toGetAllResponse(FdrAllDto fdrAllDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
