package it.gov.pagopa.fdr.util.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeParseException;

public class InstantWithoutOffsetDeserializer extends JsonDeserializer<Instant> {

  @Override
  public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {

    String value = parser.getText();
    LocalDateTime localDateTime;
    try {
      localDateTime = OffsetDateTime.parse(value).toLocalDateTime();
    } catch (DateTimeParseException e) {
      try {
        localDateTime = LocalDateTime.parse(value);
      } catch (DateTimeParseException e2) {
        localDateTime = LocalDate.parse(value).atStartOfDay();
      }
    }
    return localDateTime.toInstant(ZoneOffset.UTC);
  }
}