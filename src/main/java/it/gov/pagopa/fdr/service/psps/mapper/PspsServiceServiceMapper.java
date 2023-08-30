package it.gov.pagopa.fdr.service.psps.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentInsertEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.service.dto.FdrDto;
import it.gov.pagopa.fdr.service.dto.FdrGetCreatedDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PspsServiceServiceMapper {

  PspsServiceServiceMapper INSTANCE = Mappers.getMapper(PspsServiceServiceMapper.class);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "computedTotPayments", ignore = true)
  @Mapping(target = "computedSumPayments", ignore = true)
  FdrInsertEntity toFdrInsertEntity(FdrDto fdrDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "revision", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "updated", ignore = true)
  @Mapping(target = "refFdrId", ignore = true)
  @Mapping(target = "refFdr", ignore = true)
  @Mapping(target = "refFdrSenderPspId", ignore = true)
  @Mapping(target = "refFdrRevision", ignore = true)
  @Mapping(target = "refFdrReceiverOrganizationId", ignore = true)
  FdrPaymentInsertEntity toFdrPaymentInsertEntity(PaymentDto paymentDto);

  List<FdrPaymentInsertEntity> toFdrPaymentInsertEntityList(List<PaymentDto> paymentDto);

  @Mapping(target = "published", ignore = true)
  FdrPublishEntity toFdrPublishEntity(FdrInsertEntity fdrInsertEntity);

  List<FdrPaymentPublishEntity> toFdrPaymentPublishEntityList(
      List<FdrPaymentInsertEntity> fdrInsertEntityList);

  FdrGetCreatedDto toFdrGetCreatedDto(FdrInsertEntity fdrInsertEntity);

  List<PaymentDto> toPaymentDtoList(List<FdrPaymentInsertEntity> paymentEntities);
}
