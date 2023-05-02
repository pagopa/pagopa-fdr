package it.gov.pagopa.fdr.rest.psps.mapper;

import it.gov.pagopa.fdr.rest.psps.model.Metadata;
import it.gov.pagopa.fdr.rest.psps.model.ReportingFlowStatusEnum;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import it.gov.pagopa.fdr.service.psps.dto.AddPaymentDto;
import it.gov.pagopa.fdr.service.psps.dto.DeletePaymentDto;
import it.gov.pagopa.fdr.service.psps.dto.MetadataDto;
import it.gov.pagopa.fdr.service.psps.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.psps.dto.ReportingFlowStatusEnumDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PspsResourceServiceMapper {

  PspsResourceServiceMapper INSTANCE = Mappers.getMapper(PspsResourceServiceMapper.class);

  ReportingFlowDto toReportingFlowDto(CreateFlowRequest createRequest);

  AddPaymentDto toAddPaymentDto(AddPaymentRequest addPaymentRequest);

  DeletePaymentDto toDeletePaymentDto(DeletePaymentRequest deletePaymentRequest);

  ReportingFlowStatusEnum toReportingFlowStatusEnum(
      ReportingFlowStatusEnumDto reportingFlowStatusEnumDto);

  Metadata toMetadata(MetadataDto metadataDto);
}
