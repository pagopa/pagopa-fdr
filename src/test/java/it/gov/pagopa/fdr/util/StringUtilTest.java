package it.gov.pagopa.fdr.util;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.util.common.StringUtil;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
 class StringUtilTest {

  @Test
  @DisplayName("StringUtil - Test zip")
   void testZip() throws IOException {
    String input = "Hello, World!";
    byte[] compressed = StringUtil.zip(input);
    assertNotNull(compressed);
    assertTrue(compressed.length > 0);
  }

  @Test
  @DisplayName("StringUtil - Test sanitize")
   void testSanitize() {
    String input = "Hello\nWorld\rTest\t'\"\\";
    String expected = "Hello_World_Test____";
    String sanitized = StringUtil.sanitize(input);
    assertEquals(expected, sanitized);
  }

  @Test
  @DisplayName("StringUtil - Test sanitize with null input")
   void testSanitize_NullInput() {
    String sanitized = StringUtil.sanitize(null);
    assertNull(sanitized);
  }

  @Test
  @DisplayName("StringUtil - Test isNullOrBlank")
   void testIsNullOrBlank() {
    assertTrue(StringUtil.isNullOrBlank(null));
    assertTrue(StringUtil.isNullOrBlank(""));
    assertTrue(StringUtil.isNullOrBlank("   "));
    assertFalse(StringUtil.isNullOrBlank("Hello"));
  }

  @Test
  @DisplayName("StringUtil - Test insertCharacterAfter")
   void testInsertCharacterAfter() {
    String input = "a,b,c,d,e,f,g,h";
    String expected = "a,b,|c,d,|e,f,|g,h";
    String result = StringUtil.insertCharacterAfter(input, "|", 2, ',');
    assertEquals(expected, result);

    input = "a,b,c,d,e,f,g,h";
    expected = "a,|b,|c,|d,|e,|f,|g,|h";
    result = StringUtil.insertCharacterAfter(input, "|", 1, ',');
    assertEquals(expected, result);

    input = "a,b,c,d,e,f,g,h";
    expected = "a,b,c,d,e,f,|g,h";
    result = StringUtil.insertCharacterAfter(input, "|", 10, ',');
    assertEquals(expected, result);
  }
}
