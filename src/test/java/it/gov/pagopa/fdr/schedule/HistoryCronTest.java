package it.gov.pagopa.fdr.schedule;

import static it.gov.pagopa.fdr.test.util.TestUtil.validFlowToHistory;
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
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class HistoryCronTest {

  @InjectMock FlowToHistoryRepository flowToHistoryRepository;

  @InjectMock HistoryBlobStorageService historyBlobStorageService;

  @Inject HistoryCron historyCron;

  @Test
  void execute() {
    String dynamicFlowName = TestUtil.getDynamicFlowName();
    PanacheQuery<FlowToHistoryEntity> flowToHistory = Mockito.mock(PanacheQuery.class);
    when(flowToHistory.list()).thenReturn(List.of(validFlowToHistory(dynamicFlowName)));

    TestUtil.pspSunnyDay(dynamicFlowName);

    when(flowToHistoryRepository.findTopNEntitiesOrderByCreated(anyInt(), anyInt()))
        .thenReturn(flowToHistory);
    doNothing().when(flowToHistoryRepository).persist(any(FlowToHistoryEntity.class));

    historyCron.execute();

    verify(flowToHistoryRepository).deleteById(anyLong());
  }
}
