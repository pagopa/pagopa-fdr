package it.gov.pagopa.fdr.service.reportingFlow.mapper;

import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowPaymentRevisionEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.ReportingFlowRevisionEntity;
import it.gov.pagopa.fdr.rest.reportingFlow.model.Payment;
import it.gov.pagopa.fdr.service.reportingFlow.dto.PaymentDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.reportingFlow.dto.ReportingFlowGetDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ReportingFlowServiceMapper {

  ReportingFlowServiceMapper INSTANCE = Mappers.getMapper(ReportingFlowServiceMapper.class);

  @Mapping(source = "reporting_flow_name", target = "reportingFlowName")
  @Mapping(source = "reporting_flow_date", target = "reportingFlowDate")
  @Mapping(source = "regulation_date", target = "regulationDate")
  @Mapping(source = "bic_code_pouring_bank", target = "bicCodePouringBank")
  ReportingFlowGetDto toReportingFlowGetDto(ReportingFlowEntity reportingFlow);

  @Mapping(source = "reportingFlowName", target = "reporting_flow_name")
  @Mapping(source = "reportingFlowDate", target = "reporting_flow_date")
  @Mapping(source = "regulationDate", target = "regulation_date")
  @Mapping(source = "bicCodePouringBank", target = "bic_code_pouring_bank")
  ReportingFlowEntity toReportingFlow(ReportingFlowDto reportingFlowDto);

  @Mapping(source = "payStatus", target = "pay_status")
  @Mapping(source = "payDate", target = "pay_date")
  ReportingFlowPaymentEntity toReportingFlowPaymentEntity(PaymentDto paymentDto);

  List<ReportingFlowPaymentEntity> toReportingFlowPaymentEntityList(List<PaymentDto> paymentDto);

  List<PaymentDto> toPagamentoDtos(List<Payment> pagamentos);

  @Mapping(source = "id", target = "reporting_flow_id")
  @Mapping(target = "id", ignore = true)
  ReportingFlowRevisionEntity toReportingFlowRevision(ReportingFlowEntity reportingFlowEntity);

  @Mapping(source = "id", target = "reporting_flow_payment_id")
  @Mapping(target = "id", ignore = true)
  List<ReportingFlowPaymentRevisionEntity> toReportingFlowPaymentRevisionEntityList(
      List<ReportingFlowPaymentEntity> payment);
}
