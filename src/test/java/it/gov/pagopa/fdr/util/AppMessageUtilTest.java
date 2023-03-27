package it.gov.pagopa.fdr.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AppMessageUtilTest {

  @Test
  public void resourceBundle() {
    String str = AppMessageUtil.getMessage("fruit.name.length.max", "test", "test2");
    assertEquals("Fruit name has an invalid value [test]. Expected max length [test2]", str);
  }
}
