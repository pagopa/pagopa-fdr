package it.gov.pagopa.fdr.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentFullViewEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.test.util.MongoResource;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@QuarkusTestResource(MongoResource.class)
class PaymentFullViewRepositoryTest {

  @Inject FlowRepository flowRepository;

  @Inject PaymentFullViewRepository paymentRepository;

  @Inject UserTransaction userTransaction;

  @Test
  @DisplayName("PaymentFullViewRepositoryTest OK - findByPspAndIuvAndIur")
  void testFindByPspAndIuvAndIur() {
    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);

    RepositoryPagedResult<PaymentFullViewEntity> result =
        paymentRepository.findByPspAndIuvAndIur(
            PSP_CODE, IUV_CODE_A, IUR_CODE, null, null, null, 1, 10);

    assertNotNull(result);
    List<PaymentFullViewEntity> payments = result.getData();
    assertNotNull(payments);
    PaymentFullViewEntity payment = payments.get(0);
    assertNotNull(payment);
//    assertEquals(PSP_CODE, payment.getFlow().getPspDomainId());
    assertEquals(IUV_CODE_A, payment.getIuv());
    assertEquals(IUR_CODE, payment.getIur());
  }

  @Test
  @DisplayName("PaymentFullViewRepositoryTest OK - findByFlowId")
  void findByFlowId() {

    String flowName = TestUtil.getDynamicFlowName();
    TestUtil.pspSunnyDay(flowName, FLOW_DATE);

    RepositoryPagedResult<PaymentFullViewEntity> result = paymentRepository.findByFlowId(1L, 1, 10);

    assertNotNull(result);
    List<PaymentFullViewEntity> payments = result.getData();
    assertNotNull(payments);

    assertTrue(payments.stream().allMatch(item -> item.getId().getFlowId().equals(1L)));
  }

}
