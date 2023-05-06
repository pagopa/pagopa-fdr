//package it.gov.pagopa.fdr.adapter;
//
//import io.smallrye.common.annotation.Blocking;
//import jakarta.ws.rs.core.Response;
//import jakarta.ws.rs.ext.Provider;
//import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
//
//@Provider
//@Blocking
//public class ClientExcpetionMappers implements ResponseExceptionMapper<RuntimeException> {
//
//  @Override
//  public RuntimeException toThrowable(Response response) {
//    if (response.getStatus() == 500) {
//      throw new RuntimeException("The remote service responded with HTTP 500");
//    }
//    return null;
//  }
//}
