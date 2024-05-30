package it.gov.pagopa.fdr.exception;

import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

public enum AppErrorCodeMessageEnum implements AppErrorCodeMessageInterface {
  ERROR("0500", "system.error", RestResponse.Status.INTERNAL_SERVER_ERROR),
  BAD_REQUEST("0400", "bad.request", RestResponse.Status.BAD_REQUEST),
  BAD_REQUEST_INPUT_JSON("0401", "bad.request.inputJson", RestResponse.Status.BAD_REQUEST),
  BAD_REQUEST_INPUT_JSON_INSTANT(
      "0402", "bad.request.inputJson.instant", RestResponse.Status.BAD_REQUEST),
  BAD_REQUEST_INPUT_JSON_ENUM(
      "0403", "bad.request.inputJson.enum", RestResponse.Status.BAD_REQUEST),

  BAD_REQUEST_INPUT_JSON_DESERIALIZE_ERROR(
      "0404", "bad.request.inputJson.deserialize", RestResponse.Status.BAD_REQUEST),
  BAD_REQUEST_INPUT_JSON_NON_VALID_FORMAT(
      "0405", "bad.request.inputJson.notValidJsonFormat", RestResponse.Status.BAD_REQUEST),

  REPORTING_FLOW_NOT_FOUND("0701", "fdr.notFound", RestResponse.Status.NOT_FOUND),
  REPORTING_FLOW_ALREADY_EXIST("0702", "fdr.alreadyExist", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_WRONG_ACTION("0703", "fdr.wrongAction", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_PSP_ID_NOT_MATCH("0704", "fdr.pspId.notMatch", RestResponse.Status.BAD_REQUEST),

  REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST(
      "0705", "fdr.sameIndexInSameRequest", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX(
      "0706", "fdr.duplicateIndex", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX(
      "0707", "fdr.noMatchIndex", RestResponse.Status.BAD_REQUEST),
  PSP_UNKNOWN("0708", "pspId.unknown", RestResponse.Status.BAD_REQUEST),
  PSP_NOT_ENABLED("0709", "pspId.notEnabled", RestResponse.Status.BAD_REQUEST),
  BROKER_UNKNOWN("0710", "brokerId.unknown", RestResponse.Status.BAD_REQUEST),
  BROKER_NOT_ENABLED("0711", "brokerId.notEnabled", RestResponse.Status.BAD_REQUEST),
  CHANNEL_UNKNOWN("0712", "channelId.unknown", RestResponse.Status.BAD_REQUEST),
  CHANNEL_NOT_ENABLED("0713", "channelId.notEnabled", RestResponse.Status.BAD_REQUEST),
  CHANNEL_BROKER_WRONG_CONFIG(
      "0714", "channel.broker.wrongConfig", RestResponse.Status.BAD_REQUEST),
  CHANNEL_PSP_WRONG_CONFIG("0715", "channel.psp.wrongConfig", RestResponse.Status.BAD_REQUEST),
  EC_UNKNOWN("0716", "ecId.unknown", RestResponse.Status.BAD_REQUEST),
  EC_NOT_ENABLED("0717", "ecId.notEnabled", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_NAME_DATE_WRONG_FORMAT(
      "0718", "fdr.name-date.wrongFormat", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_NAME_PSP_WRONG_FORMAT(
      "0719", "fdr.name-psp.wrongFormat", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_NAME_NOT_MATCH("0720", "fdr.name.notMatch", RestResponse.Status.BAD_REQUEST),
  EVENT_HUB_RE_PARSE_JSON("0721", "eHub.re.parse", Status.INTERNAL_SERVER_ERROR),
  EVENT_HUB_RE_TOO_LARGE("0722", "eHub.re.tooLarge", Status.INTERNAL_SERVER_ERROR),
  REPORTING_FLOW_WRONG_TOT_PAYMENT("0723", "fdr.wrongTotPayment", RestResponse.Status.BAD_REQUEST),
  REPORTING_FLOW_WRONG_SUM_PAYMENT("0724", "fdr.wrongSumPayment", RestResponse.Status.BAD_REQUEST),
  FDR_HISTORY_VALID_JSON_ERROR(
      "0725", "fdr.fdrHistoryJsonValidationError", Status.INTERNAL_SERVER_ERROR),
  FDR_HISTORY_UPLOAD_JSON_BLOB_ERROR(
      "0726", "fdr.fdrHistoryUploadJsonError", Status.INTERNAL_SERVER_ERROR),
  FDR_HISTORY_JSON_PROCESSING_ERROR(
      "0727", "fdr.fdrHistoryJsonProcessingError", Status.INTERNAL_SERVER_ERROR),
  FDR_HISTORY_SAVE_TABLE_STORAGE_ERROR(
      "0728", "fdr.fdrHistorySaveOnTableStorageError", Status.INTERNAL_SERVER_ERROR),
  FILE_UTILS_CONVERSION_ERROR("0729", "fdr.fileUtilsConversionError", Status.INTERNAL_SERVER_ERROR),
  FILE_UTILS_FILE_NOT_FOUND("0730", "fdr.fileUtilsFileNotFound", Status.INTERNAL_SERVER_ERROR),
  COMPRESS_JSON("0731", "compress.json.error", Status.INTERNAL_SERVER_ERROR),

  EVENT_HUB_IUVRENDICONTATI_PARSE_JSON(
      "0732", "eHub.iuvrendicontati.parse", Status.INTERNAL_SERVER_ERROR),
  EVENT_HUB_IUVRENDICONTATI_TOO_LARGE(
      "0733", "eHub.iuvrendicontati.tooLarge", Status.INTERNAL_SERVER_ERROR),
  EVENT_HUB_FLUSSIRENDICONTAZIONE_PARSE_JSON(
      "0734", "eHub.flussirendicontazione.parse", Status.INTERNAL_SERVER_ERROR),
  EVENT_HUB_FLUSSIRENDICONTAZIONE_TOO_LARGE(
      "0735", "eHub.flussirendicontazione.tooLarge", Status.INTERNAL_SERVER_ERROR);

  private final String errorCode;
  private final String errorMessageKey;
  private final RestResponse.Status httpStatus;

  AppErrorCodeMessageEnum(
      String errorCode, String errorMessageKey, RestResponse.Status httpStatus) {
    this.errorCode = errorCode;
    this.errorMessageKey = errorMessageKey;
    this.httpStatus = httpStatus;
  }

  @Override
  public String errorCode() {
    return AppConstant.SERVICE_CODE_APP + "-" + errorCode;
  }

  @Override
  public String message(Object... args) {
    return AppMessageUtil.getMessage(errorMessageKey, args);
  }

  @Override
  public RestResponse.Status httpStatus() {
    return httpStatus;
  }
}
