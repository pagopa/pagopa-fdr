package it.gov.pagopa.fdr.controller.middleware;

import static org.mockito.Mockito.*;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.controller.middleware.interceptor.ClientGZIPDecodingInterceptor;
import it.gov.pagopa.fdr.util.common.FileUtil;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.GZIPInputStream;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@QuarkusTest
 class ClientGZIPDecodingInterceptorTest {

  private ClientGZIPDecodingInterceptor interceptor;

  @Mock private ReaderInterceptorContext context;
  private FileUtil fileUtil;
  @Mock private Logger logger;

  @BeforeEach
   void setUp() {
    MockitoAnnotations.openMocks(this);
    interceptor = new ClientGZIPDecodingInterceptor();
    fileUtil = new FileUtil(logger);
  }

  @Test
  @DisplayName("Test aroundReadFrom method with gzip encoding")
   void testWithGzipEncoding() throws IOException, WebApplicationException {

    InputStream inputStream =
        fileUtil.getFileFromResourceAsStream("json-test-templates/general/test.json");
    InputStream compressedGzipInputStream =
        new ByteArrayInputStream(fileUtil.compressInputStreamtoGzip(inputStream));

    MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
    headerMap.put("Content-Encoding", Collections.singletonList("gzip"));
    when(context.getHeaders()).thenReturn(headerMap);
    when(context.getInputStream()).thenReturn(compressedGzipInputStream);

    interceptor.aroundReadFrom(context);

    verify(context).setInputStream(any(GZIPInputStream.class));
    verify(context).proceed();
  }

  @Test
  @DisplayName("Test aroundReadFrom method without gzip encoding")
   void testWithoutGzipEncoding() throws IOException, WebApplicationException {
    InputStream inputStream =
        fileUtil.getFileFromResourceAsStream("json-test-templates/general/test.json");
    MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
    headerMap.put("Content-Encoding", Collections.singletonList("identity"));
    when(context.getHeaders()).thenReturn(headerMap);
    when(context.getInputStream()).thenReturn(inputStream);

    interceptor.aroundReadFrom(context);

    verify(context, never()).setInputStream(any(GZIPInputStream.class));
    verify(context).proceed();
  }
}
