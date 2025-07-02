package it.gov.pagopa.fdr.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.common.RepositoryPagedResult;
import it.gov.pagopa.fdr.repository.entity.FlowEntity;
import it.gov.pagopa.fdr.repository.enums.FlowStatusEnum;
import it.gov.pagopa.fdr.test.util.AppConstantTestHelper;
import it.gov.pagopa.fdr.test.util.PostgresResource;
import it.gov.pagopa.fdr.test.util.TestUtil;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class FlowRepositoryTest {

    @Inject
    FlowRepository flowRepository;

    @Test
    @DisplayName("FlowRepositoryTest OK - findPublishedByPspIdAndOptionalOrganizationId")
    void testFindPublishedByPspIdAndOptionalOrganizationId() {
        String flowName = TestUtil.getDynamicFlowName();
        TestUtil.pspSunnyDay(flowName);

        String pspId = AppConstantTestHelper.PSP_CODE;
        String organizationId = AppConstantTestHelper.EC_CODE;
        Instant publishedGt = Instant.now().minusSeconds(3600);
        int pageNumber = 1;
        int pageSize = 10;

        RepositoryPagedResult<FlowEntity> result =
                flowRepository.findPublishedByPspIdAndOptionalOrganizationId(
                        pspId, organizationId, publishedGt, pageNumber, pageSize);

        List<FlowEntity> entities = result.getData();
        assertFalse(entities.isEmpty());

        assertTrue(
                entities.stream()
                        .allMatch(
                                e ->
                                        e.getPspDomainId().equals(pspId)
                                                && FlowStatusEnum.PUBLISHED.name().equals(e.getStatus())
                                                && organizationId.equals(e.getOrgDomainId())
                                                && e.getPublished().isAfter(publishedGt)));
    }

    @Test
    @DisplayName("FlowRepositoryTest OK - findUnpublishedByOrganizationIdAndPspIdAndName")
    void testFindUnpublishedByOrganizationIdAndPspIdAndName() {
        String flowName = TestUtil.getDynamicFlowName();
        TestUtil.pspCreateUnpublishedFlow(flowName);

        String pspId = AppConstantTestHelper.PSP_CODE;
        String organizationId = AppConstantTestHelper.EC_CODE;

        Optional<FlowEntity> result =
                flowRepository.findUnpublishedByOrganizationIdAndPspIdAndName(
                        organizationId, pspId, flowName);

        assertTrue(result.isPresent());
        FlowEntity entity = result.get();
        assertEquals(pspId, entity.getPspDomainId());
        assertEquals(organizationId, entity.getOrgDomainId());
        assertEquals(flowName, entity.getName());
        assertNotEquals(FlowStatusEnum.PUBLISHED.name(), entity.getStatus());
    }

    @Test
    @DisplayName("FlowRepositoryTest OK - findLatestPublishedByOrganizationIdAndOptionalPspId")
    void testFindLatestPublishedByOrganizationIdAndOptionalPspId() {
        String flowName = TestUtil.getDynamicFlowName();
        TestUtil.pspSunnyDay(flowName);

        String pspId = AppConstantTestHelper.PSP_CODE;
        String organizationId = AppConstantTestHelper.EC_CODE;
        Instant publishedGt = Instant.now().minusSeconds(3600);
        Instant flowDate = Instant.now().minusSeconds(3600);
        int pageNumber = 1;
        int pageSize = 10;

        RepositoryPagedResult<FlowEntity> result =
                flowRepository.findLatestPublishedByOrganizationIdAndOptionalPspId(
                        pspId, organizationId, publishedGt, flowDate, pageNumber, pageSize);

        List<FlowEntity> entities = result.getData();
        assertFalse(entities.isEmpty());

        assertTrue(
                entities.stream()
                        .allMatch(
                                e ->
                                        e.getPspDomainId().equals(pspId)
                                                && FlowStatusEnum.PUBLISHED.name().equals(e.getStatus())
                                                && organizationId.equals(e.getOrgDomainId())
                                                && e.getPublished().isAfter(publishedGt)
                                                && e.getDate().isAfter(flowDate)
                                                && e.isLatest.equals(true)));
    }
}
