package it.gov.pagopa.fdr.repository.entity;

import io.quarkus.test.junit.QuarkusTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FlowEntityTest {

    private FlowEntity flowEntity;

    @BeforeEach
    void setUp() {
        flowEntity = new FlowEntity();
        flowEntity.setComputedTotAmount(BigDecimal.ZERO);
        flowEntity.setComputedTotPayments(0L);
    }

    @Test
    @DisplayName("FlowEntityTest addOnComputedTotAmount")
    public void addOnComputedTotAmountTest()  {

        Assertions.assertEquals(BigDecimal.ZERO, flowEntity.getComputedTotAmount());

        flowEntity.addOnComputedTotAmount(1.0);
        Assertions.assertEquals(BigDecimal.valueOf(1.0), flowEntity.getComputedTotAmount());

        flowEntity.addOnComputedTotAmount(9.0);
        Assertions.assertEquals(BigDecimal.valueOf(10.0), flowEntity.getComputedTotAmount());

        flowEntity.addOnComputedTotAmount(0.0);
        Assertions.assertEquals(BigDecimal.valueOf(10.0), flowEntity.getComputedTotAmount());

    }

    @Test
    @DisplayName("FlowEntityTest addOnComputedTotAmount")
    public void computedTotPaymentsTest()  {

        Assertions.assertEquals(0L, flowEntity.getComputedTotPayments());

        flowEntity.addOnComputedTotPayments(1);
        Assertions.assertEquals(1L, flowEntity.getComputedTotPayments());

        flowEntity.addOnComputedTotPayments(9);
        Assertions.assertEquals(10L, flowEntity.getComputedTotPayments());

        flowEntity.addOnComputedTotPayments(0);
        Assertions.assertEquals(10L, flowEntity.getComputedTotPayments());

    }
}
