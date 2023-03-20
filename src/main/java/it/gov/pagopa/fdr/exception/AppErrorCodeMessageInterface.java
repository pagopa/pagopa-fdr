package it.gov.pagopa.fdr.exception;

import org.jboss.resteasy.reactive.RestResponse;

public interface AppErrorCodeMessageInterface {
    String errorCode();
    String message(Object... args);
    RestResponse.Status httpStatus();
}
