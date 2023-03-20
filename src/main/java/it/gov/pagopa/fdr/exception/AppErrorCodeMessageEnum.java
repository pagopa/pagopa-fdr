package it.gov.pagopa.fdr.exception;

import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppUtil;
import java.util.ResourceBundle;
import org.jboss.resteasy.reactive.RestResponse;

public enum AppErrorCodeMessageEnum implements AppErrorCodeMessageInterface {

    ERROR               ("0500", "System.error", RestResponse.Status.INTERNAL_SERVER_ERROR),
    BAD_REQUEST         ("0400", "Bad.request", RestResponse.Status.BAD_REQUEST),
    FRUIT_BAD_REQUEST   ("0600", "Fruit.name.notMapped", RestResponse.Status.BAD_REQUEST),
    FRUIT_NOT_FOUND   ("0601", "Fruit.name.notFound", RestResponse.Status.NOT_FOUND);

    private final String errorCode;
    private final String errorMessageKey;
    private final RestResponse.Status httpStatus;

    AppErrorCodeMessageEnum(String errorCode, String errorMessageKey, RestResponse.Status httpStatus) {
        this.errorCode = errorCode;
        this.errorMessageKey = errorMessageKey;
        this.httpStatus = httpStatus;
    }


    @Override
    public String errorCode() {
        return AppConstant.SERVICE_CODE_APP +"-"+ errorCode;
    }

    @Override
    public String message(Object... args) {
        return AppUtil.format(
                ResourceBundle.getBundle(AppConstant.VALIDATION_MESSAGES).getString(errorMessageKey),
                args);
    }

    @Override
    public RestResponse.Status httpStatus() {
        return httpStatus;
    }
}
