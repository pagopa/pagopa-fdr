package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.common.Metadata;
import it.gov.pagopa.fdr.controller.model.flow.FlowByCICreated;
import it.gov.pagopa.fdr.controller.model.flow.FlowByCIPublished;
import it.gov.pagopa.fdr.controller.model.flow.FlowByPSP;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import java.math.BigDecimal;
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

  default List<FlowByPSP> toFlowByPSP(List<FlowEntity> list) {

    List<FlowByPSP> converted = new ArrayList<>();
    for (FlowEntity entity : list) {
      converted.add(
          FlowByPSP.builder()
              .fdr(entity.getName())
              .pspId(entity.getPspDomainId())
              .revision(entity.getRevision())
              .published(entity.getPublished())
              .build());
    }
    return converted;
  }

  default List<FlowByCICreated> toFlowByCICreated(List<FlowEntity> list) {

    List<FlowByCICreated> converted = new ArrayList<>();
    for (FlowEntity entity : list) {
      converted.add(
          FlowByCICreated.builder()
              .fdr(entity.getName())
              .organizationId(entity.getOrgDomainId())
              .revision(entity.getRevision())
              .created(entity.getCreated())
              .build());
    }
    return converted;
  }

  default List<FlowByCIPublished> toFlowByCIPublished(List<FlowEntity> list) {

    List<FlowByCIPublished> converted = new ArrayList<>();
    for (FlowEntity entity : list) {
      converted.add(
          FlowByCIPublished.builder()
              .fdr(entity.getName())
              .organizationId(entity.getOrgDomainId())
              .revision(entity.getRevision())
              .published(entity.getPublished())
              .build());
    }
    return converted;
  }

  default PaginatedFlowsResponse toPaginatedFlowResponse(
      RepositoryPagedResult<FlowEntity> paginatedResult, long pageSize, long pageNumber) {

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

  default PaginatedFlowsCreatedResponse toPaginatedFlowCreatedResponse(
      RepositoryPagedResult<FlowEntity> paginatedResult, long pageSize, long pageNumber) {

    return PaginatedFlowsCreatedResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize((int) pageSize)
                .pageNumber((int) pageNumber)
                .totPage(paginatedResult.getTotalPages())
                .build())
        .count(paginatedResult.getTotalElements())
        .data(toFlowByCICreated(paginatedResult.getData()))
        .build();
  }

  default PaginatedFlowsPublishedResponse toPaginatedFlowPublishedResponse(
      RepositoryPagedResult<FlowEntity> paginatedResult, long pageSize, long pageNumber) {

    return PaginatedFlowsPublishedResponse.builder()
        .metadata(
            Metadata.builder()
                .pageSize((int) pageSize)
                .pageNumber((int) pageNumber)
                .totPage(paginatedResult.getTotalPages())
                .build())
        .count(paginatedResult.getTotalElements())
        .data(toFlowByCIPublished(paginatedResult.getData()))
        .build();
  }

  @Mapping(source = "name", target = "fdr")
  @Mapping(source = "date", target = "fdrDate")
  @Mapping(source = "totAmount", target = "sumPayments")
  @Mapping(source = "computedTotAmount", target = "computedSumPayments")
  @Mapping(target = "sender", expression = "java(toSender(result))")
  @Mapping(target = "receiver", expression = "java(toReceiver(result))")
  SingleFlowResponse toSingleFlowResponse(FlowEntity result);

  @Mapping(source = "name", target = "fdr")
  @Mapping(source = "date", target = "fdrDate")
  @Mapping(source = "totAmount", target = "sumPayments")
  @Mapping(source = "computedTotAmount", target = "computedSumPayments")
  @Mapping(target = "sender", expression = "java(toSender(result))")
  @Mapping(target = "receiver", expression = "java(toReceiver(result))")
  SingleFlowCreatedResponse toSingleFlowCreatedResponse(FlowEntity result);

  @Mapping(source = "senderType", target = "type")
  @Mapping(source = "senderId", target = "id")
  @Mapping(source = "pspDomainId", target = "pspId")
  @Mapping(source = "senderPspName", target = "pspName")
  @Mapping(source = "senderPspBrokerId", target = "pspBrokerId")
  @Mapping(source = "senderChannelId", target = "channelId")
  @Mapping(source = "senderPassword", target = "password")
  Sender toSender(FlowEntity result);

  @Mapping(source = "receiverId", target = "id")
  @Mapping(source = "orgDomainId", target = "organizationId")
  @Mapping(source = "receiverOrganizationName", target = "organizationName")
  Receiver toReceiver(FlowEntity result);

  default FlowEntity toEntity(CreateFlowRequest request, Long revision) {

    Instant now = Instant.now();

    Sender requestSender = request.getSender();
    Receiver requestReceiver = request.getReceiver();

    FlowEntity entity = new FlowEntity();
    entity.setName(request.getFdr());
    entity.setRevision(revision);
    entity.setDate(request.getFdrDate());
    entity.setStatus(FlowStatusEnum.CREATED.name());
    entity.setIsLatest(false);
    entity.setCreated(now);
    entity.setUpdated(now);
    entity.setTotAmount(BigDecimal.valueOf(request.getSumPayments()));
    entity.setTotPayments(request.getTotPayments());
    entity.setComputedTotPayments(0L);
    entity.setComputedTotAmount(BigDecimal.valueOf(0.0));
    entity.setRegulation(request.getRegulation());
    entity.setRegulationDate(request.getRegulationDate());
    entity.setBicCodePouringBank(request.getBicCodePouringBank());
    entity.setSenderId(requestSender.getId());
    entity.setSenderType(requestSender.getType().name());
    entity.setPspDomainId(requestSender.getPspId());
    entity.setSenderPspBrokerId(requestSender.getPspBrokerId());
    entity.setSenderChannelId(requestSender.getChannelId());
    entity.setSenderPspName(requestSender.getPspName());
    entity.setSenderPassword(requestSender.getPassword());
    entity.setReceiverId(requestReceiver.getId());
    entity.setOrgDomainId(requestReceiver.getOrganizationId());
    entity.setReceiverOrganizationName(requestReceiver.getOrganizationName());

    return entity;
  }
}
