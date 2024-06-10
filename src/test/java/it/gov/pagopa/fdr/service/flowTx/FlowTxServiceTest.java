package it.gov.pagopa.fdr.service.flowTx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import it.gov.pagopa.fdr.service.flowTx.model.FlowTx;
import it.gov.pagopa.fdr.test.util.AzuriteResource;
import java.lang.reflect.Field;
import java.time.Instant;
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
class FlowTxServiceTest {
  private final ObjectMapper objectMapper;
  @InjectMock FlowTxService flowTxServiceMock;
  //  static EventHub eventHubMock;
  static FlowTx flowTx;
  Field eventHubField;
  Field objectMapperField;

  FlowTxServiceTest(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @BeforeAll
  void init() throws NoSuchFieldException, IllegalAccessException {
    Field eventHubField = FlowTxService.class.getDeclaredField("eventHub");
    eventHubField.setAccessible(true);

    Field eHubNameField = FlowTxService.class.getDeclaredField("eHubName");
    eHubNameField.setAccessible(true);

    eHubNameField.set(flowTxServiceMock, "eventHub");

    //    eventHubMock = Mockito.mock(EventHub.class);

    Field logField = FlowTxService.class.getDeclaredField("log");
    logField.setAccessible(true);

    objectMapperField = FlowTxService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);

    logField.set(flowTxServiceMock, Logger.getLogger(FlowTxService.class));
  }

  @BeforeEach
  void setReInterface() throws IllegalAccessException {
    //    eventHubField.set(flowTxServiceMock, eventHubMock);
    objectMapperField.set(flowTxServiceMock, objectMapper);
    flowTx =
        FlowTx.builder()
            // .idFlusso() //FIXME ID_FLUSSO della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .dataOraFlusso() //FIXME DATA_ORA_FLUSSO della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .insertedTimestamp() //FIXME INSERTED_TIMESTAMP della tabella
            // NODO_OFFLINE.RENDICONTAZIONE
            .dataRegolamento(Instant.now())
            .identificativoUnivocoRegolamento("")
            // .numeroTotalePagamenti() //FIXME i computed o quelli ricevuti da PSP?
            // .importoTotalePagamenti() //FIXME i computed o quelli ricevuti da PSP?
            // .idDominio() //FIXME ID_DOMINIO della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .psp() //FIXME PSP della tabella NODO_OFFLINE.RENDICONTAZIONE
            // .intPsp() //FIXME INT_PSP della tabella NODO_OFFLINE.RENDICONTAZIONE
            .uniqueId(String.format("%s%s%s", "", "", ""))
            .dataEsitoSingoloPagamentoList(null)
            .build();
    Mockito.clearInvocations(flowTxServiceMock);
    //    Mockito.clearInvocations(eventHubMock);

    Mockito.doNothing().when(flowTxServiceMock).init();
    Mockito.doCallRealMethod().when(flowTxServiceMock).sendEvent(Mockito.any());
  }

  @Test
  void testSendJsonProcessingException() throws JsonProcessingException, IllegalAccessException {
    ObjectMapper objectMapperMock = Mockito.mock(ObjectMapper.class);
    objectMapperField.set(flowTxServiceMock, objectMapperMock);
    Mockito.when(objectMapperMock.writeValueAsString(Mockito.any()))
        .thenThrow(JsonProcessingException.class);

    //    Assert.assertThrows(AppException.class, () -> flowTxServiceMock.sendEvent(flowTx));
  }

  @Test
  void testSendAllEventLT0() {
    flowTxServiceMock.sendEvent((FlowTx[]) null);
  }
}
