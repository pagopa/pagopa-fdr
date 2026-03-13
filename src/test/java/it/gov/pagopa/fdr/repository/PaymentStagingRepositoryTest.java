package it.gov.pagopa.fdr.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentFullViewEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentStagingEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@QuarkusTestResource(MongoResource.class)
class PaymentStagingRepositoryTest {

  @Inject FlowRepository flowRepository;

  @Inject PaymentStagingRepository paymentStagingRepository;

  @Inject PaymentFullViewRepository paymentFullViewRepository;

  @Inject UserTransaction userTransaction;

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

    List<PaymentStagingEntity> payments = new ArrayList<>();
    PaymentStagingEntity payment = new PaymentStagingEntity();
    payment.setId(new it.gov.pagopa.fdr.repository.entity.PaymentStagingId(flowId, 850L, flow.getOrgDomainId()));
    payment.setIuv("610901167426671");
    payment.setIur("65705570051");
    payment.setAmount(new BigDecimal("0.01"));
    payment.setPayDate(Instant.parse("2023-02-03T12:00:30.900000Z"));
    payment.setPayStatus("EXECUTED");
    payment.setTransferId(1L);
    payment.setCreated(Instant.now());
    payment.setUpdated(Instant.now());
    payments.add(payment);

    userTransaction.begin();
    paymentStagingRepository.createEntityInBulk(payments, flow.getOrgDomainId());
    userTransaction.commit();

    // Verifica con una nuova transazione
    userTransaction.begin();
    List<PaymentStagingEntity> result = paymentStagingRepository.findByFlowIdIndexesAndOrgId(flowId, java.util.Set.of(850L), flow.getOrgDomainId());
    userTransaction.commit();

    assertNotNull(result);
    assertEquals(1, result.size());

    PaymentStagingEntity inserted = result.get(0);
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

    List<PaymentStagingEntity> payments = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      PaymentStagingEntity payment = new PaymentStagingEntity();
      payment.setId(new it.gov.pagopa.fdr.repository.entity.PaymentStagingId(flowId, (long) i, flow.getOrgDomainId()));
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

    // createEntityInBulk richiede una transazione attiva (usa session.doWork con COPY binario)
    userTransaction.begin();
    paymentStagingRepository.createEntityInBulk(payments, flow.getOrgDomainId());
    userTransaction.commit();

    // Verifica con una nuova transazione
    userTransaction.begin();
    List<PaymentFullViewEntity> result = paymentFullViewRepository.findByFlowId(flowId, 1, 20).getData();
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
