package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.payment.Payment;
import it.gov.pagopa.fdr.controller.model.payment.enums.PaymentStatusEnum;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import java.math.BigDecimal;
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

  default List<Payment> toPayment(List<PaymentEntity> list) {

    List<Payment> converted = new ArrayList<>();
    for (PaymentEntity entity : list) {
      converted.add(
          Payment.builder()
              .index(entity.getId().getIndex())
              .iuv(entity.getIuv())
              .iur(entity.getIur())
              .idTransfer(entity.getTransferId())
              .pay(entity.getAmount().doubleValue())
              .payStatus(PaymentStatusEnum.valueOf(entity.getPayStatus()))
              .payDate(entity.getPayDate())
              .build());
    }
    return converted;
  }

  default PaginatedPaymentsResponse toPaginatedPaymentsResponse(
      RepositoryPagedResult<PaymentEntity> paginatedResult, long pageSize, long pageNumber) {

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

  default List<PaymentEntity> toEntity(FlowEntity flowEntity, List<Payment> payments, Instant operationTime) {
    List<PaymentEntity> converted = new LinkedList<>();
    for (Payment payment : payments) {
      PaymentEntity entity = new PaymentEntity();
      entity.setId(new it.gov.pagopa.fdr.repository.entity.PaymentId(flowEntity.getId(), payment.getIndex()));
      entity.setIuv(payment.getIuv());
      entity.setIur(payment.getIur());
      entity.setAmount(BigDecimal.valueOf(payment.getPay()));
      entity.setPayStatus(payment.getPayStatus().name());
      entity.setPayDate(payment.getPayDate());
      entity.setTransferId(payment.getIdTransfer());
      entity.setCreated(operationTime);
      entity.setUpdated(operationTime);
      converted.add(entity);
    }

    return converted;
  }
}
