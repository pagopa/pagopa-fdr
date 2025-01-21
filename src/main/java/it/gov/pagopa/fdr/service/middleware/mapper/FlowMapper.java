package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.FlowByPSP;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.repository.entity.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.flow.FdrFlowEntity;
import it.gov.pagopa.fdr.repository.entity.flow.ReceiverEntity;
import it.gov.pagopa.fdr.repository.entity.flow.SenderEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.repository.enums.SenderTypeEnum;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = ComponentModel.JAKARTA)
public interface FlowMapper {

  FlowMapper INSTANCE = Mappers.getMapper(FlowMapper.class);

  default List<FlowByPSP> toFlowByPSP(List<FdrFlowEntity> list) {

    List<FlowByPSP> converted = new ArrayList<>();
    for (FdrFlowEntity entity : list) {
      converted.add(
          FlowByPSP.builder()
              .fdr(entity.getName())
              .pspId(entity.getSender() != null ? entity.getSender().getPspId() : null)
              .revision(entity.getRevision())
              .published(entity.getPublished())
              .build());
    }
    return converted;
  }

  default PaginatedFlowsResponse toPaginatedFlowResponse(
      RepositoryPagedResult<FdrFlowEntity> paginatedResult, long pageSize, long pageNumber) {

    return PaginatedFlowsResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize((int) pageSize)
                .pageNumber((int) pageNumber)
                .totPage(paginatedResult.getTotalPages())
                .build())
        .count(paginatedResult.getTotalElements())
        .data(toFlowByPSP(paginatedResult.getData()))
        .build();
  }

  @Mapping(source = "name", target = "fdr")
  @Mapping(source = "totAmount", target = "sumPayments")
  @Mapping(source = "computedTotAmount", target = "computedSumPayments")
  SingleFlowResponse toSingleFlowResponse(FdrFlowEntity result);

  default FdrFlowEntity toEntity(CreateFlowRequest request, Long revision) {

    Instant now = Instant.now();

    Sender requestSender = request.getSender();
    SenderEntity sender = new SenderEntity();
    sender.setId(requestSender.getId());
    sender.setType(SenderTypeEnum.valueOf(requestSender.getType().name()));
    sender.setPspId(requestSender.getPspId());
    sender.setPspBrokerId(requestSender.getPspBrokerId());
    sender.setChannelId(requestSender.getChannelId());
    sender.setPspName(requestSender.getPspName());
    sender.setPassword(requestSender.getPassword());

    Receiver requestReceiver = request.getReceiver();
    ReceiverEntity receiver = new ReceiverEntity();
    receiver.setId(requestReceiver.getId());
    receiver.setOrganizationId(requestReceiver.getOrganizationId());
    receiver.setOrganizationName(requestReceiver.getOrganizationName());

    FdrFlowEntity entity = new FdrFlowEntity();
    entity.setName(request.getFdr());
    entity.setRevision(revision);
    entity.setFdrDate(request.getFdrDate());
    entity.setStatus(FlowStatusEnum.CREATED);
    entity.setCreated(now);
    entity.setUpdated(now);
    entity.setTotAmount(request.getSumPayments());
    entity.setTotPayments(request.getTotPayments());
    entity.setComputedTotPayments(0L);
    entity.setComputedTotAmount(0.0);
    entity.setRegulation(request.getRegulation());
    entity.setRegulationDate(request.getRegulationDate());
    entity.setBicCodePouringBank(request.getBicCodePouringBank());
    entity.setSender(sender);
    entity.setReceiver(receiver);
    return entity;
  }
}
