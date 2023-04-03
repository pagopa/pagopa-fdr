package it.gov.pagopa.fdr.util;

import io.quarkus.logging.Log;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AppFileUtil {

  public static void moveFile(Path source, Path target) {
    try {
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      Log.errorf(e, "File not moved from [%s] to [%s]", source.toString(), target.toString());
      throw new RuntimeException(e);
    }
  }

  public static void createDirectoryIfNotExist(Path targetDirectory) {
    boolean existDirectory = Files.exists(targetDirectory);
    if (!existDirectory) {
      try {
        Files.createDirectory(targetDirectory);
      } catch (IOException e) {
        Log.errorf(e, "Directory [%s] not created", targetDirectory.toString());
        throw new RuntimeException(e);
      }
    }
  }
}
