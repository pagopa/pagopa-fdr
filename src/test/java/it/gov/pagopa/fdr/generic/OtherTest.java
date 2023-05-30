// package it.gov.pagopa.fdr.generic;
//
// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.Matchers.equalTo;
//
// import io.quarkus.test.junit.QuarkusTest;
// import it.gov.pagopa.fdr.util.AppMessageUtil;
// import java.lang.reflect.Method;
// import java.util.Locale;
// import java.util.PropertyResourceBundle;
// import lombok.SneakyThrows;
// import org.junit.jupiter.api.Test;
//
// @QuarkusTest
// public class OtherTest {
//
//  @Test
//  @SneakyThrows
//  void test_AppMessageUtil() {
//    Method method = AppMessageUtil.class.getDeclaredMethod("getBundle", Locale.class);
//    method.setAccessible(true);
//    assertThat(PropertyResourceBundle.class, equalTo( method.invoke(null,
// Locale.ITALY).getClass()));
//  }
//
// }
