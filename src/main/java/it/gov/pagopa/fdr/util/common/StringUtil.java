package it.gov.pagopa.fdr.util.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class StringUtil {

  private StringUtil() {}

  public static byte[] zip(String str) throws IOException {
    byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream bais = new ByteArrayOutputStream(strBytes.length);
    GZIPOutputStream gzipOut = new GZIPOutputStream(bais);
    gzipOut.write(strBytes);
    gzipOut.close();
    byte[] compressed = bais.toByteArray();
    bais.close();
    return compressed;
  }

  // Replace newline, carriage return, tab, single quote, double quote, and backslash characters
  public static String sanitize(String input) {
    if (input == null) {
      return null;
    }
    return input.replaceAll("[\\n\\r\\t'\"\\\\]", "_");
  }

  public static boolean isNullOrBlank(String value) {
    return value == null || value.isBlank();
  }

  public static String insertCharacterAfter(
      String input, String newChar, int charsPerLine, char separator) {

    StringBuilder result = new StringBuilder();
    int count = 0;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      result.append(c);
      if (c == separator && count >= charsPerLine) {
        result.append(newChar);
        count = 0;
      } else {
        count++;
      }
    }
    return result.toString();
  }
}
