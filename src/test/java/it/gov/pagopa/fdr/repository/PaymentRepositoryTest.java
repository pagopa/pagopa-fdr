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
import it.gov.pagopa.fdr.test.util.PostgresResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class PaymentRepositoryTest {
    
  @Inject FlowRepository flowRepository;
  
  @Inject PaymentRepository paymentRepository;

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
  @Transactional
  void testCreateEntityInBulkSingle() throws Exception {
    // Create a flow first
    FlowEntity flow = createTestFlow();
    flowRepository.persist(flow);

    // Create a single payment
    List<PaymentEntity> payments = new ArrayList<>();
    PaymentEntity payment = new PaymentEntity();
    payment.setId(new it.gov.pagopa.fdr.repository.entity.PaymentId(flow.getId(), 850L));
    payment.setIuv("610901167426671");
    payment.setIur("65705570051");
    payment.setAmount(new BigDecimal("0.01"));
    payment.setPayDate(Instant.parse("2023-02-03T12:00:30.900000Z"));
    payment.setPayStatus("EXECUTED");
    payment.setTransferId(1L);
    payment.setCreated(Instant.now());
    payment.setUpdated(Instant.now());
    payments.add(payment);

    // Execute binary copy
    paymentRepository.createEntityInBulk(payments);

    // Verify the payment was inserted
    List<PaymentEntity> result = paymentRepository.findByFlowIdAndIndexes(flow.getId(), java.util.Set.of(850L));
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
  @Transactional
  void testCreateEntityInBulkMultiple() throws Exception {
    // Create a flow first
    FlowEntity flow = createTestFlow();
    flowRepository.persist(flow);

    // Create multiple payments
    List<PaymentEntity> payments = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      PaymentEntity payment = new PaymentEntity();
      payment.setId(new it.gov.pagopa.fdr.repository.entity.PaymentId(flow.getId(), (long) i));
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

    // Execute binary copy
    paymentRepository.createEntityInBulk(payments);

    // Verify all payments were inserted
    List<PaymentEntity> result = paymentRepository.findByFlowId(flow.getId(), 1, 20).getData();
    assertNotNull(result);
    assertEquals(10, result.size());
  }

  private FlowEntity createTestFlow() {
    FlowEntity flow = new FlowEntity();
    flow.setName("TEST_FLOW_" + System.currentTimeMillis());
    flow.setRevision(1L);
    flow.setStatus(FlowStatusEnum.INSERTED.name());
    flow.setSenderId("88888888888");
    flow.setReceiverId("12345678901");
    flow.setPspDomainId("88888888888");
    flow.setOrgDomainId("12345678901");
    flow.setCreated(Instant.now());
    flow.setUpdated(Instant.now());
    return flow;
  }
}
