package it.gov.pagopa.fdr.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class StringUtil {

  public static String zip(String str) throws IOException {
    byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream bais = new ByteArrayOutputStream(strBytes.length);
    GZIPOutputStream gzipOut = new GZIPOutputStream(bais);
    gzipOut.write(strBytes);
    gzipOut.close();
    String compressed = bais.toString(StandardCharsets.UTF_8);
    bais.close();
    return compressed;
  }
}
