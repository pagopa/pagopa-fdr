package it.gov.pagopa.fdr.rest.psps.mapper;

import it.gov.pagopa.fdr.rest.model.Metadata;
import it.gov.pagopa.fdr.rest.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.service.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.dto.MetadataDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowStatusEnumDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PspsResourceServiceMapper {

  PspsResourceServiceMapper INSTANCE = Mappers.getMapper(PspsResourceServiceMapper.class);

  ReportingFlowDto toReportingFlowDto(CreateRequest createRequest);

  AddPaymentDto toAddPaymentDto(AddPaymentRequest addPaymentRequest);

  DeletePaymentDto toDeletePaymentDto(DeletePaymentRequest deletePaymentRequest);

  ReportingFlowStatusEnum toReportingFlowStatusEnum(
      ReportingFlowStatusEnumDto reportingFlowStatusEnumDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
