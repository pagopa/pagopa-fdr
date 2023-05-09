package it.gov.pagopa.fdr.service.psps.mapper;

import it.gov.pagopa.fdr.repository.reportingFlow.FdrHistoryEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrInsertEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPaymentHistoryEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.reportingFlow.FdrPublishEntity;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PspsServiceServiceMapper {

  PspsServiceServiceMapper INSTANCE = Mappers.getMapper(PspsServiceServiceMapper.class);

  @Mapping(source = "reporting_flow_name", target = "reportingFlowName")
  @Mapping(source = "reporting_flow_date", target = "reportingFlowDate")
  @Mapping(source = "regulation_date", target = "regulationDate")
  @Mapping(source = "bic_code_pouring_bank", target = "bicCodePouringBank")
  ReportingFlowGetDto toReportingFlowGetDto(FdrInsertEntity reportingFlow);

  @Mapping(source = "reportingFlowName", target = "reporting_flow_name")
  @Mapping(source = "reportingFlowDate", target = "reporting_flow_date")
  @Mapping(source = "regulationDate", target = "regulation_date")
  @Mapping(source = "bicCodePouringBank", target = "bic_code_pouring_bank")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "totPayments", ignore = true)
  @Mapping(target = "sumPaymnents", ignore = true)
  FdrInsertEntity toReportingFlow(ReportingFlowDto reportingFlowDto);

  @Mapping(source = "payStatus", target = "pay_status")
  @Mapping(source = "payDate", target = "pay_date")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "ref_fdr_id", ignore = true)
  @Mapping(target = "ref_fdr_reporting_flow_name", ignore = true)
  @Mapping(target = "ref_fdr_reporting_sender_psp_id", ignore = true)
  FdrPaymentInsertEntity toReportingFlowPaymentEntity(PaymentDto paymentDto);

  List<FdrPaymentInsertEntity> toReportingFlowPaymentEntityList(List<PaymentDto> paymentDto);

  FdrPublishEntity toFdrPublishEntity(FdrInsertEntity fdrInsertEntity);

  List<FdrPaymentPublishEntity> toFdrPaymentPublishEntityList(
      List<FdrPaymentInsertEntity> fdrInsertEntityList);

  FdrHistoryEntity toFdrHistoryEntity(FdrInsertEntity fdrInsertEntity);

  List<FdrPaymentHistoryEntity> toFdrPaymentHistoryEntityList(
      List<FdrPaymentInsertEntity> fdrInsertEntityList);
}
