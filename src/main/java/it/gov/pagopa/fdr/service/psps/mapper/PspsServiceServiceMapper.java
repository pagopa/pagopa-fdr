package it.gov.pagopa.fdr.service.psps.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
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

  //  @Mapping(source = "reporting_flow_name", target = "reportingFlowName")
  //  @Mapping(source = "reporting_flow_date", target = "reportingFlowDate")
  //  @Mapping(source = "regulation_date", target = "regulationDate")
  //  @Mapping(source = "bic_code_pouring_bank", target = "bicCodePouringBank")
  //  @Mapping(source = "tot_payments", target = "totPayments")
  //  @Mapping(source = "sum_paymnents", target = "sumPayments")
  ReportingFlowGetDto toReportingFlowGetDto(FdrInsertEntity reportingFlow);

  //  @Mapping(source = "reportingFlowName", target = "reporting_flow_name")
  //  @Mapping(source = "reportingFlowDate", target = "reporting_flow_date")
  //  @Mapping(source = "regulationDate", target = "regulation_date")
  //  @Mapping(source = "bicCodePouringBank", target = "bic_code_pouring_bank")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "totPayments", ignore = true)
  @Mapping(target = "sumPayments", ignore = true)
  @Mapping(target = "internalNdpRead", ignore = true)
  @Mapping(target = "read", ignore = true)
  FdrInsertEntity toReportingFlow(ReportingFlowDto reportingFlowDto);

  //  @Mapping(source = "payStatus", target = "pay_status")
  //  @Mapping(source = "payDate", target = "pay_date")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "refFdrId", ignore = true)
  @Mapping(target = "refFdrReportingFlowName", ignore = true)
  @Mapping(target = "refFdrReportingSenderPspId", ignore = true)
  FdrPaymentInsertEntity toReportingFlowPaymentEntity(PaymentDto paymentDto);

  List<FdrPaymentInsertEntity> toReportingFlowPaymentEntityList(List<PaymentDto> paymentDto);

  FdrPublishEntity toFdrPublishEntity(FdrInsertEntity fdrInsertEntity);

  List<FdrPaymentPublishEntity> toFdrPaymentPublishEntityList(
      List<FdrPaymentInsertEntity> fdrInsertEntityList);

  FdrHistoryEntity toFdrHistoryEntity(FdrInsertEntity fdrInsertEntity);

  List<FdrPaymentHistoryEntity> toFdrPaymentHistoryEntityList(
      List<FdrPaymentInsertEntity> fdrInsertEntityList);
}
