package it.gov.pagopa.fdr.service.support.mapper;

import it.gov.pagopa.fdr.repository.fdr.FdrPaymentPublishEntity;
import it.gov.pagopa.fdr.service.dto.PaymentByPspIdIuvIurDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface SupportServiceServiceMapper {

  SupportServiceServiceMapper INSTANCE =
      Mappers.getMapper(SupportServiceServiceMapper.class);

  @Mapping(source = "refFdrSenderPspId", target = "pspId")
  @Mapping(source = "refFdr", target = "fdr")
  @Mapping(source = "refFdrRevision", target = "revision")
  @Mapping(source = "refFdrReceiverOrganizationId", target = "organizationId")
  PaymentByPspIdIuvIurDTO toPaymentByPspIdIuvIur(FdrPaymentPublishEntity fdrPaymentPublishEntity);
  List<PaymentByPspIdIuvIurDTO> toPaymentByPspIdIuvIurList(List<FdrPaymentPublishEntity> paymentEntities);
}
