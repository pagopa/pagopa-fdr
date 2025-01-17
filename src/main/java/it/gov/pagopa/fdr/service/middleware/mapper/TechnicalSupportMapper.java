package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.flow.FlowBySenderAndReceiver;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentPublishEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface TechnicalSupportMapper {

  TechnicalSupportMapper INSTANCE = Mappers.getMapper(TechnicalSupportMapper.class);

  @Mapping(source = "refFdrSenderPspId", target = "pspId")
  @Mapping(source = "refFdrReceiverOrganizationId", target = "organizationId")
  @Mapping(source = "refFdr", target = "fdr")
  List<FlowBySenderAndReceiver> toFlowBySenderAndReceiver(List<FdrPaymentPublishEntity> list);
}
