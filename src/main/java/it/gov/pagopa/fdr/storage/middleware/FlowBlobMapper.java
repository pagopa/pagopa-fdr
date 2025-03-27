package it.gov.pagopa.fdr.storage.middleware;

import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.storage.model.FlowBlob;
import it.gov.pagopa.fdr.storage.model.PaymentBlob;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface FlowBlobMapper {

  FlowBlobMapper INSTANCE = Mappers.getMapper(FlowBlobMapper.class);

  default FlowBlob toFlowBlob(FlowEntity flow, List<PaymentBlob> payments) {
    return FlowBlob.builder()
        .fdr(flow.name)
        .fdrDate(flow.date)
        .revision(flow.revision)
        .created(flow.created)
        .published(flow.published)
        .updated(flow.updated)
        .status(flow.status)
        .sender(
            Sender.builder()
                .type(SenderTypeEnum.valueOf(flow.senderType))
                .id(flow.senderId)
                .pspId(flow.pspDomainId)
                .pspName(flow.senderPspName)
                .pspBrokerId(flow.senderPspBrokerId)
                .channelId(flow.senderChannelId)
                .password(flow.senderPassword)
                .build())
        .receiver(
            Receiver.builder()
                .id(flow.receiverId)
                .organizationId(flow.orgDomainId)
                .organizationName(flow.receiverOrganizationName)
                .build())
        .regulation(flow.regulation)
        .regulationDate(flow.regulationDate.toString())
        .bicCodePouringBank(flow.bicCodePouringBank)
        .computedTotPayments(flow.computedTotPayments)
        .computedSumPayments(flow.computedTotAmount)
        .payments(payments)
        .build();
  }

  default PaymentBlob toPaymentBlob(PaymentEntity elem) {
    return PaymentBlob.builder()
        .index(elem.getIndex())
        .iur(elem.getIur())
        .iuv(elem.getIuv())
        .idTransfer(elem.getTransferId())
        .pay(elem.getAmount())
        .payDate(elem.getPayDate().toString())
        .payStatus(elem.getPayStatus())
        .build();
  }
}
