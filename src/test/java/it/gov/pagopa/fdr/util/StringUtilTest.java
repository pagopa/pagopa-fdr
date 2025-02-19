package it.gov.pagopa.fdr.util;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.util.common.StringUtil;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StringUtilTest {

  @Test
  public void testZip() throws IOException {
    String input = "Hello, World!";
    byte[] compressed = StringUtil.zip(input);
    assertNotNull(compressed);
    assertTrue(compressed.length > 0);
  }

  @Test
  public void testSanitize() {
    String input = "Hello\nWorld\rTest\t'\"\\";
    String expected = "Hello_World_Test____";
    String sanitized = StringUtil.sanitize(input);
    assertEquals(expected, sanitized);
  }

  @Test
  public void testSanitize_NullInput() {
    String sanitized = StringUtil.sanitize(null);
    assertNull(sanitized);
  }

  @Test
  public void testIsNullOrBlank() {
    assertTrue(StringUtil.isNullOrBlank(null));
    assertTrue(StringUtil.isNullOrBlank(""));
    assertTrue(StringUtil.isNullOrBlank("   "));
    assertFalse(StringUtil.isNullOrBlank("Hello"));
  }
}
