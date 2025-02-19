package it.gov.pagopa.fdr.util.common;

import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FileUtil {

  private final Logger log;

  public FileUtil(Logger log) {
    this.log = log;
  }

  public static String getStringFromJsonFile(String fileName) {
    ClassLoader classLoader = FileUtil.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);
    String result = null;
    if (inputStream == null) {
      throw new AppException(AppErrorCodeMessageEnum.ERROR);
    }

    try (InputStreamReader streamReader =
            new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)) {
      result = reader.lines().collect(Collectors.joining());
    } catch (IOException e) {
      throw new AppException(AppErrorCodeMessageEnum.ERROR);
    }

    return result;
  }

  public String getStringFromResourceAsString(String fileName) {
    InputStream inputStream = getFileFromResourceAsStream(fileName);
    return convertToString(inputStream);
  }

  public InputStream getFileFromResourceAsStream(String fileName) {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);
    if (inputStream == null) {
      log.errorf("Error reading file: [%s]", fileName);
      throw new AppException(AppErrorCodeMessageEnum.ERROR);
    } else {
      return inputStream;
    }
  }

  public String convertToString(InputStream is) {
    try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)) {
      return reader.lines().collect(Collectors.joining());
    } catch (IOException e) {
      log.error("Error converting InputStream to String", e);
      throw new AppException(AppErrorCodeMessageEnum.ERROR);
    }
  }

  public byte[] compressInputStreamtoGzip(@Nonnull final InputStream inputStream) {
    try {
      ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();

      try (inputStream;
          GZIPOutputStream gzipOutput = new GZIPOutputStream(bytesOutput)) {
        byte[] buffer = new byte[10240];
        for (int length = 0; (length = inputStream.read(buffer)) != -1; ) {
          gzipOutput.write(buffer, 0, length);
        }
      }
      return bytesOutput.toByteArray();
    } catch (IOException e) {
      log.error("Error compressing InputStream to Gzip", e);
      throw new AppException(AppErrorCodeMessageEnum.ERROR);
    }
  }
}
