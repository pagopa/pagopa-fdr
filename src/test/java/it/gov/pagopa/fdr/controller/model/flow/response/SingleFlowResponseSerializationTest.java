package it.gov.pagopa.fdr.controller.model.flow.response;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.controller.model.flow.enums.ReportingFlowStatusEnum;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SingleFlowResponseSerializationTest {

  @Inject
  ObjectMapper objectMapper;

  @Test
  void singleFlowResponse_shouldKeepUtcInstantsAndPreserveRegulationDateAroundMidnight()
      throws Exception {

    SingleFlowResponse response =
        SingleFlowResponse.builder()
            .revision(1L)
            .status(ReportingFlowStatusEnum.PUBLISHED)
            .created(Instant.parse("2026-03-24T23:30:00Z"))
            .updated(Instant.parse("2026-03-24T23:31:00Z"))
            .published(Instant.parse("2026-03-24T23:32:00Z"))
            .fdr("FDR-MIDNIGHT-TEST")
            .fdrDate(Instant.parse("2026-03-24T23:30:00Z"))
            .regulation("REG-MIDNIGHT")
            .regulationDate(LocalDate.of(2026, 3, 24))
            .build();

    String json = objectMapper.writeValueAsString(response);

    assertTrue(json.contains("\"created\":\"2026-03-24T23:30:00Z\""), json);
    assertTrue(json.contains("\"updated\":\"2026-03-24T23:31:00Z\""), json);
    assertTrue(json.contains("\"published\":\"2026-03-24T23:32:00Z\""), json);
    assertTrue(json.contains("\"fdrDate\":\"2026-03-24T23:30:00Z\""), json);

    assertFalse(json.contains("2026-03-25T00:30:00+01:00"), json);
    assertFalse(json.contains("+01:00"), json);
    assertFalse(json.contains("+02:00"), json);

    assertTrue(json.contains("\"regulationDate\":\"2026-03-24\""), json);
    assertFalse(json.contains("\"regulationDate\":\"2026-03-24T"), json);
    assertFalse(json.contains("\"regulationDate\":\"2026-03-23"), json);
  }

  @Test
  void singleFlowCreatedResponse_shouldKeepUtcInstantsAndPreserveRegulationDateAroundMidnight()
      throws Exception {

    SingleFlowCreatedResponse response =
        SingleFlowCreatedResponse.builder()
            .revision(1L)
            .status(ReportingFlowStatusEnum.CREATED)
            .created(Instant.parse("2026-03-24T23:30:00Z"))
            .updated(Instant.parse("2026-03-24T23:31:00Z"))
            .fdr("FDR-MIDNIGHT-TEST")
            .fdrDate(Instant.parse("2026-03-24T23:30:00Z"))
            .regulation("REG-MIDNIGHT")
            .regulationDate(LocalDate.of(2026, 3, 24))
            .build();

    String json = objectMapper.writeValueAsString(response);

    assertTrue(json.contains("\"created\":\"2026-03-24T23:30:00Z\""), json);
    assertTrue(json.contains("\"updated\":\"2026-03-24T23:31:00Z\""), json);
    assertTrue(json.contains("\"fdrDate\":\"2026-03-24T23:30:00Z\""), json);

    assertFalse(json.contains("2026-03-25T00:30:00+01:00"), json);
    assertFalse(json.contains("+01:00"), json);
    assertFalse(json.contains("+02:00"), json);

    assertTrue(json.contains("\"regulationDate\":\"2026-03-24\""), json);
    assertFalse(json.contains("\"regulationDate\":\"2026-03-24T"), json);
    assertFalse(json.contains("\"regulationDate\":\"2026-03-23"), json);
  }
}