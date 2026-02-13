package it.gov.pagopa.fdr.repository;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class PaymentRepositoryBinaryTest {

  @Inject PaymentRepository paymentRepository;
  @Inject FlowRepository flowRepository;

  @Test
  @DisplayName("Test createEntityInBulkCopyBinary - Single Payment")
  @Transactional
  void testCreateEntityInBulkCopyBinarySingle() throws Exception {
    // Create a flow first
    FlowEntity flow = createTestFlow();
    flowRepository.persist(flow);

    // Create a single payment
    List<PaymentEntity> payments = new ArrayList<>();
    PaymentEntity payment = new PaymentEntity();
    payment.setFlowId(flow.getId());
    payment.setIuv("610901167426671");
    payment.setIur("65705570051");
    payment.setIndex(850L);
    payment.setAmount(new BigDecimal("0.01"));
    payment.setPayDate(Instant.parse("2023-02-03T12:00:30.900000Z"));
    payment.setPayStatus("EXECUTED");
    payment.setTransferId(1L);
    payment.setCreated(Instant.now());
    payment.setUpdated(Instant.now());
    payments.add(payment);

    // Execute binary copy
    paymentRepository.createEntityInBulkCopyBinary(payments);

    // Verify the payment was inserted
    List<PaymentEntity> result = paymentRepository.findByFlowIdAndIndexes(flow.getId(), java.util.Set.of(850L));
    assertNotNull(result);
    assertEquals(1, result.size());

    PaymentEntity inserted = result.get(0);
    assertEquals("610901167426671", inserted.getIuv());
    assertEquals("65705570051", inserted.getIur());
    assertEquals(850L, inserted.getIndex());
    assertEquals(0, new BigDecimal("0.01").compareTo(inserted.getAmount()));
    assertEquals("EXECUTED", inserted.getPayStatus());
    assertEquals(1L, inserted.getTransferId());
  }

  @Test
  @DisplayName("Test createEntityInBulkCopyBinary - Multiple Payments")
  @Transactional
  void testCreateEntityInBulkCopyBinaryMultiple() throws Exception {
    // Create a flow first
    FlowEntity flow = createTestFlow();
    flowRepository.persist(flow);

    // Create multiple payments
    List<PaymentEntity> payments = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      PaymentEntity payment = new PaymentEntity();
      payment.setFlowId(flow.getId());
      payment.setIuv("IUV" + i);
      payment.setIur("IUR" + i);
      payment.setIndex((long) i);
      payment.setAmount(new BigDecimal("10.50"));
      payment.setPayDate(Instant.now());
      payment.setPayStatus("EXECUTED");
      payment.setTransferId(1L);
      payment.setCreated(Instant.now());
      payment.setUpdated(Instant.now());
      payments.add(payment);
    }

    // Execute binary copy
    paymentRepository.createEntityInBulkCopyBinary(payments);

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

