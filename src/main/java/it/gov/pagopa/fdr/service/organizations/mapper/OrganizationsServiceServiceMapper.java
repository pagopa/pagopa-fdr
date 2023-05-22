package it.gov.pagopa.fdr.service.organizations.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface OrganizationsServiceServiceMapper {

  OrganizationsServiceServiceMapper INSTANCE =
      Mappers.getMapper(OrganizationsServiceServiceMapper.class);

  //  @Mapping(source = "reporting_flow_name", target = "reportingFlowName")
  //  @Mapping(source = "reporting_flow_date", target = "reportingFlowDate")
  //  @Mapping(source = "regulation_date", target = "regulationDate")
  //  @Mapping(source = "bic_code_pouring_bank", target = "bicCodePouringBank")
  //  @Mapping(source = "tot_payments", target = "totPayments")
  //  @Mapping(source = "sum_paymnents", target = "sumPayments")
  ReportingFlowGetDto toReportingFlowGetDto(FdrPublishEntity reportingFlow);

  //  @Mapping(source = "pay_status", target = "payStatus")
  //  @Mapping(source = "pay_date", target = "payDate")
  PaymentDto toPagamentoDto(FdrPaymentPublishEntity paymentEntity);

  List<PaymentDto> toPagamentoDtos(List<FdrPaymentPublishEntity> paymentEntities);
}
