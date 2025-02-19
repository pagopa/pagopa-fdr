package it.gov.pagopa.fdr.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.util.logging.AppMessageUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AppMessageUtilTest {

  public static final String TEST_STRING = "test";

  @Test
  @DisplayName("AppMessageUtilTest OK - resourceBundle")
  void resourceBundle() {
    String str = AppMessageUtil.getMessage("app.description");
    assertEquals("FDR - Flussi di rendicontazione", str);
  }
  @Test
  @DisplayName("AppMessageUtilTest OK - logProcess")
  void logProcess() {
    String str = AppMessageUtil.logProcess(TEST_STRING);
    assertEquals("Process "+TEST_STRING, str);
  }
  @Test
  @DisplayName("AppMessageUtilTest OK - logValidate")
  void logValidate() {
    String str = AppMessageUtil.logValidate(TEST_STRING);
    assertEquals("Validate "+TEST_STRING, str);
  }
  @Test
  @DisplayName("AppMessageUtilTest OK - logExecute")
  void logExecute() {
    String str = AppMessageUtil.logExecute(TEST_STRING);
    assertEquals("Execute "+TEST_STRING, str);
  }
  @Test
  @DisplayName("AppMessageUtilTest OK - logErrorMessage")
  void logErrorMessage() {
    String str = AppMessageUtil.logErrorMessage(TEST_STRING);
    assertEquals("Error [message:"+TEST_STRING+"]", str);
  }

}
