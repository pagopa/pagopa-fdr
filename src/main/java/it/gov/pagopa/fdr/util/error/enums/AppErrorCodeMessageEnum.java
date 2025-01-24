package it.gov.pagopa.fdr.util.error.enums;

import it.gov.pagopa.fdr.util.constant.AppConstant;
import it.gov.pagopa.fdr.util.logging.AppMessageUtil;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

public enum AppErrorCodeMessageEnum {
  ERROR("0500", "system.error", RestResponse.Status.INTERNAL_SERVER_ERROR, "-"),

  // syntactic errors
  BAD_REQUEST("1000", "bad.request", RestResponse.Status.BAD_REQUEST, "-"),
  BAD_REQUEST_INPUT_JSON("1001", "bad.request.inputJson", RestResponse.Status.BAD_REQUEST, "-"),
  BAD_REQUEST_INPUT_JSON_INSTANT(
      "1002", "bad.request.inputJson.instant", RestResponse.Status.BAD_REQUEST, "-"),
  BAD_REQUEST_INPUT_JSON_ENUM(
      "1003", "bad.request.inputJson.enum", RestResponse.Status.BAD_REQUEST, "-"),
  BAD_REQUEST_INPUT_JSON_DESERIALIZE_ERROR(
      "1004", "bad.request.inputJson.deserialize", RestResponse.Status.BAD_REQUEST, "-"),
  BAD_REQUEST_INPUT_JSON_NON_VALID_FORMAT(
      "1005", "bad.request.inputJson.notValidJsonFormat", RestResponse.Status.BAD_REQUEST, "-"),

  // semantic errors - cached configuration
  PSP_UNKNOWN("2000", "pspId.unknown", RestResponse.Status.BAD_REQUEST, "-"),
  PSP_NOT_ENABLED("2001", "pspId.notEnabled", RestResponse.Status.BAD_REQUEST, "-"),
  BROKER_UNKNOWN("2002", "brokerId.unknown", RestResponse.Status.BAD_REQUEST, "-"),
  BROKER_NOT_ENABLED("2003", "brokerId.notEnabled", RestResponse.Status.BAD_REQUEST, "-"),
  CHANNEL_UNKNOWN("2004", "channelId.unknown", RestResponse.Status.BAD_REQUEST, "-"),
  CHANNEL_NOT_ENABLED("2005", "channelId.notEnabled", RestResponse.Status.BAD_REQUEST, "-"),
  CHANNEL_BROKER_WRONG_CONFIG(
      "2006", "channel.broker.wrongConfig", RestResponse.Status.BAD_REQUEST, "-"),
  CHANNEL_PSP_WRONG_CONFIG("2007", "channel.psp.wrongConfig", RestResponse.Status.BAD_REQUEST, "-"),
  EC_UNKNOWN("2008", "ecId.unknown", RestResponse.Status.BAD_REQUEST, "-"),
  EC_NOT_ENABLED("2009", "ecId.notEnabled", RestResponse.Status.BAD_REQUEST, "-"),

  // semantic checks - prepublish-related checks
  REPORTING_FLOW_NOT_FOUND("3001", "fdr.notFound", RestResponse.Status.NOT_FOUND, "-"),
  REPORTING_FLOW_ALREADY_EXIST("3002", "fdr.alreadyExist", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_WRONG_ACTION("3003", "fdr.wrongAction", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_PSP_ID_NOT_MATCH(
      "3004", "fdr.pspId.notMatch", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST(
      "3005", "fdr.sameIndexInSameRequest", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX(
      "3006", "fdr.duplicateIndex", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX(
      "3007", "fdr.noMatchIndex", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_NAME_DATE_WRONG_FORMAT(
      "3008", "fdr.name-date.wrongFormat", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_NAME_PSP_WRONG_FORMAT(
      "3009", "fdr.name-psp.wrongFormat", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_NAME_NOT_MATCH("3010", "fdr.name.notMatch", RestResponse.Status.BAD_REQUEST, "-"),
  // EVENT_HUB_RE_PARSE_JSON("2021", "eHub.re.parse", Status.INTERNAL_SERVER_ERROR, "-"),
  // EVENT_HUB_RE_TOO_LARGE("0722", "eHub.re.tooLarge", Status.INTERNAL_SERVER_ERROR, "-"),

  // semantic checks - publish-related checks
  REPORTING_FLOW_WRONG_TOT_PAYMENT(
      "4001", "fdr.wrongTotPayment", RestResponse.Status.BAD_REQUEST, "-"),
  REPORTING_FLOW_WRONG_SUM_PAYMENT(
      "4002", "fdr.wrongSumPayment", RestResponse.Status.BAD_REQUEST, "-"),
  // FDR_HISTORY_VALID_JSON_ERROR("0725", "fdr.fdrHistoryJsonValidationError",
  // Status.INTERNAL_SERVER_ERROR, "-"),
  // FDR_HISTORY_UPLOAD_JSON_BLOB_ERROR("0726", "fdr.fdrHistoryUploadJsonError",
  // Status.INTERNAL_SERVER_ERROR, "-"),
  // FDR_HISTORY_JSON_PROCESSING_ERROR("0727", "fdr.fdrHistoryJsonProcessingError",
  // Status.INTERNAL_SERVER_ERROR, "-"),
  // FDR_HISTORY_SAVE_TABLE_STORAGE_ERROR("0728", "fdr.fdrHistorySaveOnTableStorageError",
  // Status.INTERNAL_SERVER_ERROR, "-"),

  // other checks
  FILE_UTILS_CONVERSION_ERROR(
      "5000", "fdr.fileUtilsConversionError", Status.INTERNAL_SERVER_ERROR, "-"),
  FILE_UTILS_FILE_NOT_FOUND("5001", "fdr.fileUtilsFileNotFound", Status.INTERNAL_SERVER_ERROR, "-"),
  COMPRESS_JSON("5002", "compress.json.error", Status.INTERNAL_SERVER_ERROR, "-");

  private final String errorCode;
  private final String errorMessageKey;
  private final RestResponse.Status httpStatus;
  private final String openAPIDescription;

  AppErrorCodeMessageEnum(
      String errorCode,
      String errorMessageKey,
      RestResponse.Status httpStatus,
      String openAPIDescription) {

    this.errorCode = errorCode;
    this.errorMessageKey = errorMessageKey;
    this.httpStatus = httpStatus;
    this.openAPIDescription = openAPIDescription;
  }

  public String errorCode() {
    return AppConstant.SERVICE_CODE_APP + "-" + errorCode;
  }

  public String message(Object... args) {
    return AppMessageUtil.getMessage(errorMessageKey, args);
  }

  public RestResponse.Status httpStatus() {
    return httpStatus;
  }

  public String openAPIDescription() {
    return this.openAPIDescription;
  }
}
