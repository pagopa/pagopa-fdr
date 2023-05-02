package it.gov.pagopa.fdr.service.organizations.mapper;

import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentEntity;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface OrganizationsServiceServiceMapper {

  OrganizationsServiceServiceMapper INSTANCE =
      Mappers.getMapper(OrganizationsServiceServiceMapper.class);

  @Mapping(source = "reporting_flow_name", target = "reportingFlowName")
  @Mapping(source = "reporting_flow_date", target = "reportingFlowDate")
  @Mapping(source = "regulation_date", target = "regulationDate")
  @Mapping(source = "bic_code_pouring_bank", target = "bicCodePouringBank")
  ReportingFlowGetDto toReportingFlowGetDto(ReportingFlowEntity reportingFlow);

  List<PaymentDto> toPagamentoDtos(List<ReportingFlowPaymentEntity> paymentEntities);
}
