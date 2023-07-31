package it.gov.pagopa.fdr.service.organizations.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentHistoryEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface InternalOrganizationsServiceServiceMapper {

  InternalOrganizationsServiceServiceMapper INSTANCE =
      Mappers.getMapper(InternalOrganizationsServiceServiceMapper.class);

  FdrGetDto toFdrGetDto(FdrPublishEntity fdrPublishEntity);

  FdrGetDto toFdrGetDtoByHistory(FdrHistoryEntity fdrHistoryEntity);

  PaymentDto toPaymentDto(FdrPaymentPublishEntity paymentEntity);

  List<PaymentDto> toPaymentDtoList(List<FdrPaymentPublishEntity> paymentEntities);

  PaymentDto historyToPaymentDto(FdrPaymentHistoryEntity paymentEntity);

  List<PaymentDto> historyToPaymentDtoList(List<FdrPaymentHistoryEntity> paymentEntities);
}
