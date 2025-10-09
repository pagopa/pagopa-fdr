package it.gov.pagopa.fdr.repository;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class PaymentRepositoryTest {

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
}
