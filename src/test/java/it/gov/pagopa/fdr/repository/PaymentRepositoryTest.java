package it.gov.pagopa.fdr.repository;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@QuarkusTestResource(MongoResource.class)
class PaymentRepositoryTest {
    
  @Inject FlowRepository flowRepository;
  
  @Inject PaymentRepository paymentRepository;

  @Inject UserTransaction userTransaction;

  @Test
  @DisplayName("PaymentRepositoryTest OK - findByPspAndIuvAndIur")
  void testFindByPspAndIuvAndIur() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);

    RepositoryPagedResult<PaymentEntity> result =
        paymentRepository.findByPspAndIuvAndIur(
            PSP_CODE, IUV_CODE_A, IUR_CODE, null, null, null, 1, 10);

    assertNotNull(result);
    List<PaymentEntity> payments = result.getData();
    assertNotNull(payments);
    PaymentEntity payment = payments.get(0);
    assertNotNull(payment);
    assertEquals(PSP_CODE, payment.getFlow().getPspDomainId());
    assertEquals(IUV_CODE_A, payment.getIuv());
    assertEquals(IUR_CODE, payment.getIur());
  }

  @Test
  @DisplayName("PaymentRepositoryTest OK - findByFlowId")
  void findByFlowId() {

    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);

    RepositoryPagedResult<PaymentEntity> result = paymentRepository.findByFlowId(1L, 1, 10);

    assertNotNull(result);
    List<PaymentEntity> payments = result.getData();
    assertNotNull(payments);

    assertTrue(payments.stream().allMatch(item -> item.getFlow().getId().equals(1L)));
  }

  @Test
  @DisplayName("Test CreateEntityInBulk - Single Payment")
  void testCreateEntityInBulkSingle() throws Exception {
    // Il binary copy PostgreSQL usa la stessa connessione della transazione JTA
    // ma richiede che il flow sia visibile (committato) prima di inserire il payment.
    // Usiamo UserTransaction per committare il flow esplicitamente.
    FlowEntity flow = createTestFlow();

    userTransaction.begin();
    flowRepository.persist(flow);
    userTransaction.commit(); // Flow ora committato e visibile al COPY

    assertNotNull(flow.getId());
    Long flowId = flow.getId();

    List<PaymentEntity> payments = new ArrayList<>();
    PaymentEntity payment = new PaymentEntity();
    payment.setId(new it.gov.pagopa.fdr.repository.entity.PaymentId(flowId, 850L));
    payment.setIuv("610901167426671");
    payment.setIur("65705570051");
    payment.setAmount(new BigDecimal("0.01"));
    payment.setPayDate(Instant.parse("2023-02-03T12:00:30.900000Z"));
    payment.setPayStatus("EXECUTED");
    payment.setTransferId(1L);
    payment.setCreated(Instant.now());
    payment.setUpdated(Instant.now());
    payments.add(payment);

    paymentRepository.createEntityInBulk(payments);

    // Verifica con una nuova transazione
    userTransaction.begin();
    List<PaymentEntity> result = paymentRepository.findByFlowIdAndIndexes(flowId, java.util.Set.of(850L));
    userTransaction.commit();

    assertNotNull(result);
    assertEquals(1, result.size());

    PaymentEntity inserted = result.get(0);
    assertEquals("610901167426671", inserted.getIuv());
    assertEquals("65705570051", inserted.getIur());
    assertEquals(850L, inserted.getId().getIndex());
    assertEquals(0, new BigDecimal("0.01").compareTo(inserted.getAmount()));
    assertEquals("EXECUTED", inserted.getPayStatus());
    assertEquals(1L, inserted.getTransferId());
  }

  @Test
  @DisplayName("Test CreateEntityInBulk - Multiple Payments")
  void testCreateEntityInBulkMultiple() throws Exception {
    FlowEntity flow = createTestFlow();

    userTransaction.begin();
    flowRepository.persist(flow);
    userTransaction.commit(); // Flow ora committato e visibile al COPY

    assertNotNull(flow.getId());
    Long flowId = flow.getId();

    List<PaymentEntity> payments = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      PaymentEntity payment = new PaymentEntity();
      payment.setId(new it.gov.pagopa.fdr.repository.entity.PaymentId(flowId, (long) i));
      payment.setIuv("IUV" + i);
      payment.setIur("IUR" + i);
      payment.setAmount(new BigDecimal("10.50"));
      payment.setPayDate(Instant.now());
      payment.setPayStatus("EXECUTED");
      payment.setTransferId(1L);
      payment.setCreated(Instant.now());
      payment.setUpdated(Instant.now());
      payments.add(payment);
    }

    // createEntityInBulk è annotato @Transactional(REQUIRES_NEW)
    paymentRepository.createEntityInBulk(payments);

    // Verifica con una nuova transazione
    userTransaction.begin();
    List<PaymentEntity> result = paymentRepository.findByFlowId(flowId, 1, 20).getData();
    userTransaction.commit();

    assertNotNull(result);
    assertEquals(10, result.size());
  }

  private FlowEntity createTestFlow() {
    FlowEntity flow = new FlowEntity();
    flow.setName("TEST_FLOW_" + System.currentTimeMillis());
    flow.setDate(Instant.now());
    flow.setRevision(1L);
    flow.setStatus(FlowStatusEnum.INSERTED.name());
    flow.setIsLatest(true);
    flow.setSenderId("88888888888");
    flow.setSenderType("LEGAL_PERSON");
    flow.setSenderPspName("Test PSP");
    flow.setSenderPspBrokerId("88888888888");
    flow.setSenderChannelId("88888888888_01");
    flow.setReceiverId("12345678901");
    flow.setReceiverOrganizationName("Test Org");
    flow.setPspDomainId("88888888888");
    flow.setOrgDomainId("12345678901");
    flow.setRegulation("SEPA");
    flow.setRegulationDate(Instant.now());
    flow.setTotAmount(BigDecimal.ZERO);
    flow.setTotPayments(0L);
    flow.setComputedTotAmount(BigDecimal.ZERO);
    flow.setComputedTotPayments(0L);
    flow.setCreated(Instant.now());
    flow.setUpdated(Instant.now());
    return flow;
  }
}
