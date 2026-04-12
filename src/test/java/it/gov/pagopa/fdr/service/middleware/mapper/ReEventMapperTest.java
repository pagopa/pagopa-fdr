package it.gov.pagopa.fdr.service.middleware.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

import it.gov.pagopa.fdr.repository.entity.re.ReEventEntity;
import it.gov.pagopa.fdr.service.model.re.ReEvent;

class ReEventMapperTest {

  @Test
  void toEntity_shouldBuildPartitionKeyUsingEuropeRomeTimezone() {
    ReEvent reEvent = ReEvent.builder()
        .created(Instant.parse("2026-03-24T23:30:00Z"))
        .sessionId("SESSION1")
        .build();

    ReEventEntity entity = ReEventMapper.INSTANCE.toEntity(reEvent);

    assertNotNull(entity);
    assertEquals("2026-03-25", entity.getPartitionKey());
    assertNotNull(entity.getUniqueId());
    assertTrue(entity.getUniqueId().startsWith("2026-03-25_"));
  }
  
  @Test
  void toEntity_shouldKeepSameDateWhenUtcAndEuropeRomeAreSameDay() {
    ReEvent reEvent = ReEvent.builder()
        .created(Instant.parse("2026-03-24T10:15:00Z"))
        .sessionId("SESSION1")
        .build();

    ReEventEntity entity = ReEventMapper.INSTANCE.toEntity(reEvent);

    assertNotNull(entity);
    assertEquals("2026-03-24", entity.getPartitionKey());
    assertNotNull(entity.getUniqueId());
    assertTrue(entity.getUniqueId().startsWith("2026-03-24_"));
  }
}