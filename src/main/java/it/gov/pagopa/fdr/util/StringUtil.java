package it.gov.pagopa.fdr.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class StringUtil {

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
    if (input == null) return null;
    return input.replaceAll("[\\n\\r\\t'\"\\\\]", "_");
  }
}
