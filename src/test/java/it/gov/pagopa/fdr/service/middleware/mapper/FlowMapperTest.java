package it.gov.pagopa.fdr.service.middleware.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.controller.model.flow.FlowByCICreated;
import it.gov.pagopa.fdr.controller.model.flow.FlowByCIPublished;
import it.gov.pagopa.fdr.controller.model.flow.FlowByPSP;
import it.gov.pagopa.fdr.controller.model.flow.Receiver;
import it.gov.pagopa.fdr.controller.model.flow.Sender;
import it.gov.pagopa.fdr.controller.model.flow.enums.SenderTypeEnum;
import it.gov.pagopa.fdr.controller.model.flow.request.CreateFlowRequest;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsPublishedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowCreatedResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@QuarkusTest
class FlowMapperTest {

  private final FlowMapper flowMapper = Mappers.getMapper(FlowMapper.class);
  private final FlowEntity flowEntity = new FlowEntity();

  @BeforeEach
  void setUp() {
    flowEntity.setName("testName");
    flowEntity.setPspDomainId("testPspId");
    flowEntity.setRevision(1L);
    flowEntity.setPublished(Instant.now());
    flowEntity.setOrgDomainId("testOrgId");
    flowEntity.setCreated(Instant.now());
    flowEntity.setDate(Instant.now());
    flowEntity.setTotAmount(BigDecimal.TEN);
    flowEntity.setComputedTotAmount(BigDecimal.ONE);
    flowEntity.setSenderType(SenderTypeEnum.LEGAL_PERSON.name());
    flowEntity.setSenderId("testId");
    flowEntity.setSenderPspName("testPspName");
    flowEntity.setSenderPspBrokerId("testPspBrokerId");
    flowEntity.setSenderChannelId("testChannelId");
    flowEntity.setSenderPassword("testPassword");
    flowEntity.setReceiverId("testId");
    flowEntity.setReceiverOrganizationName("testOrgName");
  }

  @Test
  void testToFlowByPSP() {

    List<FlowByPSP> result = flowMapper.toFlowByPSP(List.of(flowEntity));

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("testName", result.get(0).getFdr());
    assertEquals("testPspId", result.get(0).getPspId());
    assertEquals(1L, result.get(0).getRevision());
  }

  @Test
  void testToFlowByCICreated() {

    List<FlowByCICreated> result = flowMapper.toFlowByCICreated(List.of(flowEntity));

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("testName", result.get(0).getFdr());
    assertEquals("testOrgId", result.get(0).getOrganizationId());
    assertEquals(1L, result.get(0).getRevision());
  }

  @Test
  void testToFlowByCIPublished() {

    List<FlowByCIPublished> result = flowMapper.toFlowByCIPublished(List.of(flowEntity));

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("testName", result.get(0).getFdr());
    assertEquals("testOrgId", result.get(0).getOrganizationId());
    assertEquals(1L, result.get(0).getRevision());
  }

  @Test
  void testToPaginatedFlowResponse() {

    RepositoryPagedResult<FlowEntity> paginatedResult = mock(RepositoryPagedResult.class);
    when(paginatedResult.getTotalPages()).thenReturn(1);
    when(paginatedResult.getTotalElements()).thenReturn(1L);
    when(paginatedResult.getData()).thenReturn(List.of(flowEntity));

    PaginatedFlowsResponse result = flowMapper.toPaginatedFlowResponse(paginatedResult, 10, 1);

    assertNotNull(result);
    assertEquals(1, result.getMetadata().getTotPage());
    assertEquals(1, result.getCount());
    assertEquals(1, result.getData().size());
  }

  @Test
  void testToPaginatedFlowCreatedResponse() {

    RepositoryPagedResult<FlowEntity> paginatedResult = mock(RepositoryPagedResult.class);
    when(paginatedResult.getTotalPages()).thenReturn(1);
    when(paginatedResult.getTotalElements()).thenReturn(1L);
    when(paginatedResult.getData()).thenReturn(List.of(flowEntity));

    PaginatedFlowsCreatedResponse result =
        flowMapper.toPaginatedFlowCreatedResponse(paginatedResult, 10, 1);

    assertNotNull(result);
    assertEquals(1, result.getMetadata().getTotPage());
    assertEquals(1, result.getCount());
    assertEquals(1, result.getData().size());
  }

  @Test
  void testToPaginatedFlowPublishedResponse() {

    RepositoryPagedResult<FlowEntity> paginatedResult = mock(RepositoryPagedResult.class);
    when(paginatedResult.getTotalPages()).thenReturn(1);
    when(paginatedResult.getTotalElements()).thenReturn(1L);
    when(paginatedResult.getData()).thenReturn(List.of(flowEntity));

    PaginatedFlowsPublishedResponse result =
        flowMapper.toPaginatedFlowPublishedResponse(paginatedResult, 10, 1);

    assertNotNull(result);
    assertEquals(1, result.getMetadata().getTotPage());
    assertEquals(1, result.getCount());
    assertEquals(1, result.getData().size());
  }

  @Test
  void testToSingleFlowResponse() {

    SingleFlowResponse result = flowMapper.toSingleFlowResponse(flowEntity);

    assertNotNull(result);
    assertEquals("testName", result.getFdr());
    assertEquals(10.0, result.getSumPayments());
    assertEquals(1.0, result.getComputedSumPayments());
  }

  @Test
  void testToSingleFlowCreatedResponse() {

    SingleFlowCreatedResponse result = flowMapper.toSingleFlowCreatedResponse(flowEntity);

    assertNotNull(result);
    assertEquals("testName", result.getFdr());
    assertEquals(10.0, result.getSumPayments());
    assertEquals(1.0, result.getComputedSumPayments());
  }

  @Test
  void testToSender() {

    Sender result = flowMapper.toSender(flowEntity);

    assertNotNull(result);
    assertEquals(SenderTypeEnum.LEGAL_PERSON.name(), result.getType().name());
    assertEquals("testId", result.getId());
    assertEquals("testPspId", result.getPspId());
    assertEquals("testPspName", result.getPspName());
    assertEquals("testPspBrokerId", result.getPspBrokerId());
    assertEquals("testChannelId", result.getChannelId());
    assertEquals("testPassword", result.getPassword());
  }

  @Test
  void testToReceiver() {

    Receiver result = flowMapper.toReceiver(flowEntity);

    assertNotNull(result);
    assertEquals("testId", result.getId());
    assertEquals("testOrgId", result.getOrganizationId());
    assertEquals("testOrgName", result.getOrganizationName());
  }

  @Test
  void testToEntity() {
    Sender sender =
        Sender.builder()
            .id("testSenderId")
            .type(SenderTypeEnum.LEGAL_PERSON)
            .pspId("testPspId")
            .pspBrokerId("testPspBrokerId")
            .channelId("testChannelId")
            .pspName("testPspName")
            .password("testPassword")
            .build();
    Receiver receiver =
        Receiver.builder()
            .id("testReceiverId")
            .organizationId("testOrgId")
            .organizationName("testOrgName")
            .build();
    CreateFlowRequest request =
        CreateFlowRequest.builder()
            .fdr("testFdr")
            .fdrDate(Instant.now())
            .sumPayments(100.0)
            .totPayments(10L)
            .regulation("testRegulation")
            .regulationDate(LocalDate.from(Instant.now()))
            .bicCodePouringBank("testBicCode")
            .sender(sender)
            .receiver(receiver)
            .build();

    FlowEntity result = flowMapper.toEntity(request, 1L);

    assertNotNull(result);
    assertEquals("testFdr", result.getName());
    assertEquals(1L, result.getRevision());
    assertEquals("testSenderId", result.getSenderId());
    assertEquals("testReceiverId", result.getReceiverId());
  }
}
