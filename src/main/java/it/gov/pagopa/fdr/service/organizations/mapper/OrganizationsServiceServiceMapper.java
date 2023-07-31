package it.gov.pagopa.fdr.service.organizations.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.repository.fdr.FdrPublishEntity;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.PaymentDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface OrganizationsServiceServiceMapper {

  OrganizationsServiceServiceMapper INSTANCE =
      Mappers.getMapper(OrganizationsServiceServiceMapper.class);

  FdrGetDto toFdrGetDto(FdrPublishEntity fdrPublishEntity);

  PaymentDto toPaymentDto(FdrPaymentPublishEntity paymentEntity);

  List<PaymentDto> toPaymentDtoList(List<FdrPaymentPublishEntity> paymentEntities);
}
