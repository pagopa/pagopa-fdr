package it.gov.pagopa.fdr.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@JsonPropertyOrder({"errorId","httpStatusCode","httpStatusDescription","appErrorCode","errors"})
public class ErrorResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorId;

    private final int httpStatusCode;
    private final String httpStatusDescription;

    private final String appErrorCode;
    private final List<ErrorMessage> errors;

    public ErrorResponse(String errorId, int httpStatusCode, String httpStatusDescription, String appErrorCode, ErrorMessage errorMessage) {
        this.errorId = errorId;
        this.httpStatusCode = httpStatusCode;
        this.httpStatusDescription = httpStatusDescription;
        this.appErrorCode = appErrorCode;
        this.errors = List.of(errorMessage);
    }

    public ErrorResponse(int httpStatusCode, String httpStatusDescription, String appErrorCode, ErrorMessage errorMessage) {
        this.errorId = null;
        this.httpStatusCode = httpStatusCode;
        this.httpStatusDescription = httpStatusDescription;
        this.appErrorCode = appErrorCode;
        this.errors = List.of(errorMessage);
    }

    public ErrorResponse(int httpStatusCode, String httpStatusDescription, String appErrorCode, List<ErrorMessage> errorMessages) {
        this.errorId = null;
        this.httpStatusCode = httpStatusCode;
        this.httpStatusDescription = httpStatusDescription;
        this.appErrorCode = appErrorCode;
        this.errors = errorMessages;
    }




    @Getter
    @EqualsAndHashCode
    @JsonPropertyOrder({"path","message"})
    public static class ErrorMessage {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final String path;
        private final String message;

        public ErrorMessage(String path, String message) {
            this.path = path;
            this.message = message;
        }

        public ErrorMessage(String message) {
            this.path = null;
            this.message = message;
        }

    }

}
