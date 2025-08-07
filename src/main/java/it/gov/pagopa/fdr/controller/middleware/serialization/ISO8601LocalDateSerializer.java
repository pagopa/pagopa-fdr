package it.gov.pagopa.fdr.controller.middleware.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ISO8601LocalDateSerializer extends LocalDateSerializer {

  @Override
  public void serialize(LocalDate date, JsonGenerator generator, SerializerProvider provider)
      throws IOException {
    String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    generator.writeString(formattedDate);
  }
}
