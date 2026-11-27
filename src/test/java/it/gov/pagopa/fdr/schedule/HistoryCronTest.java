package it.gov.pagopa.fdr.schedule;

import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.FLOW_DATE;
import static it.gov.pagopa.fdr.test.util.TestUtil.validFlowToHistory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.repository.FlowToHistoryRepository;
import it.gov.pagopa.fdr.repository.entity.FlowToHistoryEntity;
import it.gov.pagopa.fdr.storage.HistoryBlobStorageService;
import it.gov.pagopa.fdr.test.util.TestUtil;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@QuarkusTest
class HistoryCronTest {

  @InjectMock FlowToHistoryRepository flowToHistoryRepository;

  @InjectMock HistoryBlobStorageService historyBlobStorageService;

  @Inject HistoryCron historyCron;
  @ConfigProperty(name = "schedule.history.retries")
  int maxRetries;


  @Test
  void execute() throws IOException {
    String dynamicFlowName = TestUtil.getDynamicFlowName();

    @SuppressWarnings("unchecked")
    PanacheQuery<FlowToHistoryEntity> flowToHistory =
        mock(PanacheQuery.class);

    when(flowToHistory.list())
        .thenReturn(List.of(validFlowToHistory(dynamicFlowName)));

    TestUtil.pspSunnyDay(dynamicFlowName, FLOW_DATE);

    when(flowToHistoryRepository.findTopNEntitiesOrderByCreated(
        anyInt(), anyInt()))
        .thenReturn(flowToHistory);

    doNothing()
        .when(flowToHistoryRepository)
        .persist(any(FlowToHistoryEntity.class));

    historyCron.execute();

    InOrder inOrder =
        inOrder(historyBlobStorageService, flowToHistoryRepository);

    inOrder.verify(historyBlobStorageService)
        .saveJsonFile(any(), any(FlowToHistoryEntity.class));

    inOrder.verify(flowToHistoryRepository)
        .deleteById(anyLong());
  }

  @Test
  void execute_uploadFailure_shouldNotDeleteHistoryRecord() throws IOException {

    String dynamicFlowName = TestUtil.getDynamicFlowName();

    FlowToHistoryEntity entity =
        validFlowToHistory(dynamicFlowName);

    @SuppressWarnings("unchecked")
    PanacheQuery<FlowToHistoryEntity> flowToHistory =
        mock(PanacheQuery.class);

    when(flowToHistory.list())
        .thenReturn(List.of(entity));

    TestUtil.pspSunnyDay(dynamicFlowName, FLOW_DATE);

    when(flowToHistoryRepository.findTopNEntitiesOrderByCreated(
        anyInt(), anyInt()))
        .thenReturn(flowToHistory);

    doNothing()
        .when(flowToHistoryRepository)
        .persist(any(FlowToHistoryEntity.class));

    RuntimeException uploadError =
        new RuntimeException("Azure Blob upload failed");

    doThrow(uploadError)
        .when(historyBlobStorageService)
        .saveJsonFile(any(), any(FlowToHistoryEntity.class));

    assertThrows(
        RuntimeException.class,
        () -> historyCron.execute());

    verify(historyBlobStorageService)
        .saveJsonFile(any(), any(FlowToHistoryEntity.class));

    verify(flowToHistoryRepository, never())
        .deleteById(anyLong());
  }
  @Test
  void execute_uploadFailure_shouldUpdateRetries() throws IOException {

    String dynamicFlowName = TestUtil.getDynamicFlowName();

    FlowToHistoryEntity entity =
        validFlowToHistory(dynamicFlowName);

    entity.setRetries(0);

    @SuppressWarnings("unchecked")
    PanacheQuery<FlowToHistoryEntity> flowToHistory =
        mock(PanacheQuery.class);

    when(flowToHistory.list())
        .thenReturn(List.of(entity));

    TestUtil.pspSunnyDay(dynamicFlowName, FLOW_DATE);

    when(flowToHistoryRepository.findTopNEntitiesOrderByCreated(
        anyInt(), anyInt()))
        .thenReturn(flowToHistory);

    doNothing()
        .when(flowToHistoryRepository)
        .persist(any(FlowToHistoryEntity.class));

    doThrow(new RuntimeException("Azure Blob upload failed"))
        .when(historyBlobStorageService)
        .saveJsonFile(any(), any(FlowToHistoryEntity.class));

    assertThrows(
        RuntimeException.class,
        () -> historyCron.execute());

    assertEquals(1, entity.getRetries());

    verify(flowToHistoryRepository, never())
        .deleteById(anyLong());
  }
  @Test
  void execute_maxRetriesReached() throws IOException {

    String dynamicFlowName = TestUtil.getDynamicFlowName();

    FlowToHistoryEntity entity =
        validFlowToHistory(dynamicFlowName);

    entity.setRetries(maxRetries - 1);

    @SuppressWarnings("unchecked")
    PanacheQuery<FlowToHistoryEntity> flowToHistory =
        mock(PanacheQuery.class);

    when(flowToHistory.list())
        .thenReturn(List.of(entity));

    TestUtil.pspSunnyDay(dynamicFlowName, FLOW_DATE);

    when(flowToHistoryRepository.findTopNEntitiesOrderByCreated(
        anyInt(), anyInt()))
        .thenReturn(flowToHistory);

    doNothing()
        .when(flowToHistoryRepository)
        .persist(any(FlowToHistoryEntity.class));

    doThrow(new RuntimeException("Azure Blob upload failed"))
        .when(historyBlobStorageService)
        .saveJsonFile(any(), any(FlowToHistoryEntity.class));

    assertThrows(
        RuntimeException.class,
        () -> historyCron.execute());

    assertEquals(maxRetries, entity.getRetries());

    verify(historyBlobStorageService)
        .saveJsonFile(any(), any(FlowToHistoryEntity.class));

    verify(flowToHistoryRepository, never())
        .deleteById(anyLong());
  }


}
