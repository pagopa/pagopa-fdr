package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.enums.PaymentStatusEnum;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.repository.entity.payment.FdrPaymentEntity;
import it.gov.pagopa.fdr.repository.entity.payment.ReferencedFdrEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  default List<Payment> toPayment(List<FdrPaymentEntity> list) {

    List<Payment> converted = new ArrayList<>();
    for (FdrPaymentEntity entity : list) {
      converted.add(
          Payment.builder()
              .index(entity.getIndex())
              .iuv(entity.getIuv())
              .iur(entity.getIur())
              .idTransfer(entity.getTransferId())
              .pay(entity.getAmount())
              .payStatus(PaymentStatusEnum.valueOf(entity.getPayStatus().name()))
              .payDate(entity.getPayDate())
              .build());
    }
    return converted;
  }

  default PaginatedPaymentsResponse toPaginatedPaymentsResponse(
      RepositoryPagedResult<FdrPaymentEntity> paginatedResult, long pageSize, long pageNumber) {

    return PaginatedPaymentsResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize((int) pageSize)
                .pageNumber((int) pageNumber)
                .totPage(paginatedResult.getTotalPages())
                .build())
        .count(paginatedResult.getTotalElements())
        .data(toPayment(paginatedResult.getData()))
        .build();
  }

  default List<FdrPaymentEntity> toEntity(
      FdrFlowEntity flowEntity, List<Payment> payments, Instant operationTime) {

    ReferencedFdrEntity referencedFdrEntity = new ReferencedFdrEntity();
    referencedFdrEntity.setId(flowEntity.id);
    referencedFdrEntity.setName(flowEntity.getName());
    referencedFdrEntity.setRevision(flowEntity.getRevision());
    referencedFdrEntity.setSenderPspId(flowEntity.getSender().getPspId());
    referencedFdrEntity.setReceiverOrganizationId(flowEntity.getReceiver().getOrganizationId());

    List<FdrPaymentEntity> converted = new LinkedList<>();
    for (Payment payment : payments) {
      FdrPaymentEntity entity = new FdrPaymentEntity();
      entity.setIuv(payment.getIuv());
      entity.setIur(payment.getIur());
      entity.setIndex(payment.getIndex());
      entity.setAmount(payment.getPay());
      entity.setPayStatus(
          it.gov.pagopa.fdr.repository.enums.PaymentStatusEnum.valueOf(
              payment.getPayStatus().name()));
      entity.setPayDate(payment.getPayDate());
      entity.setTransferId(payment.getIdTransfer());
      entity.setCreated(operationTime);
      entity.setUpdated(operationTime);
      entity.setRefFdr(referencedFdrEntity);
      converted.add(entity);
    }

    return converted;
  }
}
