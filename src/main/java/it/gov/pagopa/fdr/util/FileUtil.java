package it.gov.pagopa.fdr.util;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FileUtil {
  private final Logger log;

  public FileUtil(Logger log) {
    this.log = log;
  }

  public InputStream getFileFromResourceAsStream(String fileName) {
    // The class loader that loaded the class
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);
    // the stream holding the file content
    if (inputStream == null) {
      log.errorf("Error reading file: [%s]", fileName);
      throw new IllegalArgumentException("file not found! " + fileName);
    } else {
      return inputStream;
    }
  }

  public String convertToString(InputStream is) {
    try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)) {
      return IOUtils.toString(reader);
    } catch (IOException e) {
      log.error("Error converting InputStream to String", e);
      throw new RuntimeException(e);
    }
  }
}
