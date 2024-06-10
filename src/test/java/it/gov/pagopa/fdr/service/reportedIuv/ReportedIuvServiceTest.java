package it.gov.pagopa.fdr.service.reportedIuv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.gov.pagopa.fdr.service.reportedIuv.model.ReportedIuv;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
@QuarkusTestResource(AzuriteResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportedIuvServiceTest {
  private final ObjectMapper objectMapper;
  @InjectMock ReportedIuvService reportedIuvServiceMock;
  //  static EventHub eventHubMock;
  static List<ReportedIuv> reportedIuv;
  Field eventHubField;
  Field objectMapperField;

  ReportedIuvServiceTest(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @BeforeAll
  void init() throws NoSuchFieldException, IllegalAccessException {
    Field eventHubField = ReportedIuvService.class.getDeclaredField("eventHub");
    eventHubField.setAccessible(true);

    Field eHubNameField = ReportedIuvService.class.getDeclaredField("eHubName");
    eHubNameField.setAccessible(true);

    eHubNameField.set(reportedIuvServiceMock, "eventHub");

    //    eventHubMock = Mockito.mock(EventHub.class);

    Field logField = ReportedIuvService.class.getDeclaredField("log");
    logField.setAccessible(true);

    objectMapperField = ReportedIuvService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);

    logField.set(reportedIuvServiceMock, Logger.getLogger(ReportedIuvService.class));
  }

  @BeforeEach
  void setReInterface() throws IllegalAccessException {
    //    eventHubField.set(reportedIuvServiceMock, eventHubMock);
    objectMapperField.set(reportedIuvServiceMock, objectMapper);
    reportedIuv =
        List.of(
            ReportedIuv.builder()
                .identificativoUnivocoVersamento("")
                .identificativoUnivocoRiscossione("")
                .singoloImportoPagato(BigDecimal.valueOf(10))
                .codiceEsitoSingoloPagamento(0)
                .dataEsitoSingoloPagamento(Instant.now())
                .indiceDatiSingoloPagamento("")
                .identificativoFlusso("")
                .dataOraFlusso(Instant.now())
                .identificativoDominio("")
                .identificativoPSP("")
                .identificativoIntermediarioPSP("")
                .uniqueId(UUID.randomUUID().toString())
                .insertedTimestamp(Instant.now())
                .build());
    Mockito.clearInvocations(reportedIuvServiceMock);
    //    Mockito.clearInvocations(eventHubMock);

    Mockito.doNothing().when(reportedIuvServiceMock).init();
    Mockito.doCallRealMethod().when(reportedIuvServiceMock).sendEvent(Mockito.anyList());
  }

  @Test
  void testSendJsonProcessingException() throws JsonProcessingException, IllegalAccessException {
    ObjectMapper objectMapperMock = Mockito.mock(ObjectMapper.class);
    objectMapperField.set(reportedIuvServiceMock, objectMapperMock);
    Mockito.when(objectMapperMock.writeValueAsString(Mockito.any()))
        .thenThrow(JsonProcessingException.class);

    //      Assert.assertThrows(AppException.class, () ->
    // reportedIuvServiceMock.sendEvent(reportedIuv));
  }

  @Test
  void testSendAllEventLT0() {
    reportedIuvServiceMock.sendEvent(null);
  }
}
