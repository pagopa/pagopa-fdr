package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.flow.FlowBySenderAndReceiver;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface TechnicalSupportMapper {

  TechnicalSupportMapper INSTANCE = Mappers.getMapper(TechnicalSupportMapper.class);

  List<FlowBySenderAndReceiver> toFlowBySenderAndReceiver(List<FdrPaymentEntity> list);

  @Mapping(source = "refFdr.senderPspId", target = "pspId")
  @Mapping(source = "refFdr.receiverOrganizationId", target = "organizationId")
  @Mapping(source = "refFdr.name", target ="fdr" )
  @Mapping(source = "refFdr.revision", target = "revision")
  FlowBySenderAndReceiver map (FdrPaymentEntity elem);


}
