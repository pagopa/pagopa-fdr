package it.gov.pagopa.fdr.service.psps.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.service.dto.FdrDto;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PspsServiceServiceMapper {

  PspsServiceServiceMapper INSTANCE = Mappers.getMapper(PspsServiceServiceMapper.class);

  FdrGetDto toFdrGetDto(FdrInsertEntity reportingFlow);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "totPayments", ignore = true)
  @Mapping(target = "sumPayments", ignore = true)
  FdrInsertEntity toFdrInsertEntity(FdrDto fdrDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "refFdrId", ignore = true)
  @Mapping(target = "refFdr", ignore = true)
  @Mapping(target = "refFdrSenderPspId", ignore = true)
  @Mapping(target = "refFdrRevision", ignore = true)
  FdrPaymentInsertEntity toFdrPaymentInsertEntity(PaymentDto paymentDto);

  List<FdrPaymentInsertEntity> toFdrPaymentInsertEntityList(List<PaymentDto> paymentDto);

  FdrPublishEntity toFdrPublishEntity(FdrInsertEntity fdrInsertEntity);

  List<FdrPaymentPublishEntity> toFdrPaymentPublishEntityList(
      List<FdrPaymentInsertEntity> fdrInsertEntityList);

  FdrHistoryEntity toFdrHistoryEntity(FdrInsertEntity fdrInsertEntity);

  List<FdrPaymentHistoryEntity> toFdrPaymentHistoryEntityList(
      List<FdrPaymentInsertEntity> fdrInsertEntityList);
}
