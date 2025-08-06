package it.gov.pagopa.fdr.controller.middleware.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MonetarySerializer extends JsonSerializer<Double> {

  private final DecimalFormat decimalFormat;

  public MonetarySerializer() {

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    symbols.setDecimalSeparator('.');
    decimalFormat = new DecimalFormat("0.00", symbols);
  }

  @Override
  public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {

    gen.writeNumber(decimalFormat.format(value));
  }
}
