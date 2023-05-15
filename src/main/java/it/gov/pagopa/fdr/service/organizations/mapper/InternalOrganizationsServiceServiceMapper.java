package it.gov.pagopa.fdr.service.organizations.mapper;

import it.gov.pagopa.fdr.repository.reportingFlow.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPublishEntity;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface InternalOrganizationsServiceServiceMapper {

  InternalOrganizationsServiceServiceMapper INSTANCE =
      Mappers.getMapper(InternalOrganizationsServiceServiceMapper.class);

  @Mapping(source = "reporting_flow_name", target = "reportingFlowName")
  @Mapping(source = "reporting_flow_date", target = "reportingFlowDate")
  @Mapping(source = "regulation_date", target = "regulationDate")
  @Mapping(source = "bic_code_pouring_bank", target = "bicCodePouringBank")
  @Mapping(source = "tot_payments", target = "totPayments")
  @Mapping(source = "sum_paymnents", target = "sumPaymnents")
  ReportingFlowGetDto toReportingFlowGetDto(FdrPublishEntity reportingFlow);

  @Mapping(source = "pay_status", target = "payStatus")
  @Mapping(source = "pay_date", target = "payDate")
  PaymentDto toPagamentoDto(FdrPaymentPublishEntity paymentEntity);

  List<PaymentDto> toPagamentoDtos(List<FdrPaymentPublishEntity> paymentEntities);
}
