package it.gov.pagopa.fdr.service.middleware.mapper;

import it.gov.pagopa.fdr.controller.model.flow.FlowBySenderAndReceiver;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.entity.PaymentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TechnicalSupportMapperTest {

    private final TechnicalSupportMapper mapper = Mappers.getMapper(TechnicalSupportMapper.class);
    private final PaymentEntity paymentEntity = new PaymentEntity();

    @BeforeEach
    public void setUp() {
        paymentEntity.setFlow(new FlowEntity());
        paymentEntity.getFlow().setPspDomainId("psp123");
        paymentEntity.getFlow().setOrgDomainId("org123");
        paymentEntity.getFlow().setName("flowName");
        paymentEntity.getFlow().setRevision(1L);
    }

    @Test
    void testToFlowBySenderAndReceiverList() {

        List<FlowBySenderAndReceiver> result = mapper.toFlowBySenderAndReceiver(Collections.singletonList(paymentEntity));

        assertNotNull(result);
        assertEquals(1, result.size());
        FlowBySenderAndReceiver elem= result.get(0);
        assertNotNull(elem);
        assertEquals("psp123", elem.getPspId());
        assertEquals("org123", elem.getOrganizationId());
        assertEquals("flowName", elem.getFdr());
        assertEquals(1, elem.getRevision());
    }

    @Test
    void testToFlowBySenderAndReceiver() {

        FlowBySenderAndReceiver result = mapper.toFlowBySenderAndReceiver(paymentEntity);

        assertNotNull(result);
        assertEquals("psp123", result.getPspId());
        assertEquals("org123", result.getOrganizationId());
        assertEquals("flowName", result.getFdr());
        assertEquals(1, result.getRevision());
    }
}
