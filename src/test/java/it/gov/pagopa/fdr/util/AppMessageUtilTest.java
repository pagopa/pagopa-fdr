package it.gov.pagopa.fdr.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.util.logging.AppMessageUtil;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AppMessageUtilTest {

  @Test
  void resourceBundle() {
    String str = AppMessageUtil.getMessage("app.description");
    assertEquals("FDR - Flussi di rendicontazione", str);
  }
}
