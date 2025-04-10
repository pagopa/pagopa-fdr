package it.gov.pagopa.fdr.controller.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.quarkus.arc.ArcUndeclaredThrowableException;
import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.controller.middleware.exceptionhandler.ExceptionMappers;
import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.logging.AppMessageUtil;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

@QuarkusTest
class ExceptionMapperTest {

  private final String errorMessage = AppMessageUtil.getMessage("system.error");
  private final String invalidJsonErrorMessage =
      AppMessageUtil.getMessage("bad.request.inputJson.notValidJsonFormat");
  private ExceptionMappers exceptionMappers;

  @BeforeEach
  void setUp() {
    Logger logger = mock(Logger.class);
    exceptionMappers = new ExceptionMappers(logger);
  }

  @Test
  @DisplayName("Test ExceptionMappers mapThrowable")
  void testMapThrowable() {
    Throwable exception = new RuntimeException("Test exception");

    RestResponse<ErrorResponse> response = exceptionMappers.mapThrowable(exception);

    assertNotNull(response);
    assertEquals(RestResponse.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertNotNull(response.getEntity());
    assertEquals(AppErrorCodeMessageEnum.ERROR.errorCode(), response.getEntity().getAppErrorCode());
    assertEquals(
        RestResponse.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        response.getEntity().getHttpStatusDescription());
    assertEquals(errorMessage, response.getEntity().getErrors().get(0).getMessage());
  }

  @Test
  @DisplayName("Test ExceptionMappers JsonMappingException")
  void testMapWebApplicationException_withJsonMappingException() {
    JsonMappingException jsonMappingException = new JsonMappingException(null, "Test exception");
    jsonMappingException.withCause(new RuntimeException());

    WebApplicationException webApplicationException =
        new WebApplicationException(jsonMappingException);

    Response response = exceptionMappers.mapWebApplicationException(webApplicationException);

    assertNotNull(response);
    assertEquals(RestResponse.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
    assertNotNull(errorResponse);
    assertInstanceOf(ErrorResponse.class, errorResponse);
    assertNotNull(errorResponse.getErrors());
    assertEquals(1, errorResponse.getErrors().size());
    assertEquals(
        invalidJsonErrorMessage,
        response.readEntity(ErrorResponse.class).getErrors().get(0).getMessage());
  }

  @Test
  @DisplayName("Test ExceptionMappers JsonParseException")
  void testMapWebApplicationException_withJsonParseException() {
    JsonParseException jsonParseException = new JsonParseException("Test exception");
    WebApplicationException webApplicationException =
        new WebApplicationException(jsonParseException);

    Response response = exceptionMappers.mapWebApplicationException(webApplicationException);

    assertNotNull(response);
    assertEquals(RestResponse.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
    assertNotNull(errorResponse);
    assertInstanceOf(ErrorResponse.class, errorResponse);
    assertNotNull(errorResponse.getErrors());
    assertEquals(1, errorResponse.getErrors().size());
    assertEquals(
        invalidJsonErrorMessage,
        response.readEntity(ErrorResponse.class).getErrors().get(0).getMessage());
  }

  @ParameterizedTest
  @CsvSource({"flow_revision_idx,400", "flow_to_historicization_idx,404", "unmapped_idx,500"})
  @DisplayName("Test ExceptionMappers ConstraintViolationException")
  void testMapArcUndeclaredThrowableException_withConstraintViolationException(
      String constraintName, int statusCode) {

    jakarta.transaction.RollbackException rollbackException =
        new jakarta.transaction.RollbackException() {
          @Override
          public synchronized Throwable getCause() {
            return new org.hibernate.exception.ConstraintViolationException(
                "fake exception", new SQLException(), constraintName);
          }
        };
    ArcUndeclaredThrowableException arcUndeclaredThrowableException =
        new ArcUndeclaredThrowableException("fake ARC undeclared exception", rollbackException);

    RestResponse<ErrorResponse> response =
        exceptionMappers.mapArcUndeclaredThrowableException(arcUndeclaredThrowableException);

    assertNotNull(response);
    assertEquals(statusCode, response.getStatus());
  }

  @ParameterizedTest
  @CsvSource({
    "violates unique constraint \"payment_by_fdr_idx\",400",
    "violates unique constraint \"unmapped_idx\",500"
  })
  @DisplayName("Test ExceptionMappers BatchUpdateException")
  void testMapArcUndeclaredThrowableException_withBatchUpdateException(
      String cause, int statusCode) {

    BatchUpdateException batchUpdateException =
        new BatchUpdateException(new PSQLException(cause, PSQLState.UNIQUE_VIOLATION));
    ArcUndeclaredThrowableException arcUndeclaredThrowableException =
        new ArcUndeclaredThrowableException("fake ARC undeclared exception", batchUpdateException);

    RestResponse<ErrorResponse> response =
        exceptionMappers.mapArcUndeclaredThrowableException(arcUndeclaredThrowableException);

    assertNotNull(response);
    assertEquals(statusCode, response.getStatus());
  }

  @Test
  @DisplayName("Test ExceptionMappers OtherException")
  void testMapWebApplicationException_withOtherException() {
    WebApplicationException webApplicationException =
        new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());

    Response response = exceptionMappers.mapWebApplicationException(webApplicationException);

    assertNotNull(response);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }
}
