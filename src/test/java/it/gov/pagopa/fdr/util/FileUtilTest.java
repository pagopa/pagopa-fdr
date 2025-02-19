package it.gov.pagopa.fdr.util;

import static it.gov.pagopa.fdr.test.util.AppConstantTestHelper.TEST_TEMPLATE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.util.common.FileUtil;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.error.exception.common.AppException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FileUtilTest {

  private FileUtil fileUtil;
  private Logger logger;

  @BeforeEach
  void setUp() {
    logger = mock(Logger.class);
    fileUtil = new FileUtil(logger);
  }

  @Test
  @DisplayName("FileUtil getFileFromResourceAsStream test with existent file")
  void testGetFileFromResourceAsStream_FileExists() {
    InputStream result =
        fileUtil.getFileFromResourceAsStream("json-test-templates/general/test.json");
    assertNotNull(result);
  }

  @Test
  @DisplayName("FileUtil getFileFromResourceAsStream test with non existent file")
  void testGetFileFromResourceAsStream_FileNotFound() {
    String fileName = "nonexistent.json";

    AppException exception =
        assertThrows(
            AppException.class,
            () -> {
              fileUtil.getFileFromResourceAsStream(fileName);
            });

    assertEquals(AppErrorCodeMessageEnum.ERROR, exception.getCodeMessage());
    verify(logger).errorf("Error reading file: [%s]", fileName);
  }

  @Test
  @DisplayName("FileUtil convertToString test with existent content")
  void testConvertToString_Success() {
    String content = "test content";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());

    String result = fileUtil.convertToString(inputStream);

    assertEquals(content, result);
  }

  @Test
  @DisplayName("FileUtil getStringFromResourceAsString test with existent content")
  void getStringFromResource_Success() {

    String content = "{  \"test\": \"test\",  \"test2\": \"test2\"}";

    String result = fileUtil.getStringFromResourceAsString(TEST_TEMPLATE_PATH);

    assertEquals(content, result);
  }
}
