package it.gov.pagopa.fdr.controller.middleware.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;
import java.time.LocalDate;

public class MandatoryLocalDateDeserializer extends JsonDeserializer<LocalDate> {

  @Override
  public LocalDate deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {

    String rawDate = parser.getText();
    if (rawDate == null) {
      throw InvalidFormatException.from(
          parser,
          "Date expected in format \"yyyy-MM-dd\" but passed null value.",
          null,
          LocalDate.class);
    }
    if (rawDate.length() < 10) {
      throw InvalidFormatException.from(
          parser,
          String.format("Date expected in format \"yyyy-MM-dd\" but passed [%s] date.", rawDate),
          rawDate,
          LocalDate.class);
    }
    return LocalDate.parse(rawDate.substring(0, 10));
  }
}
