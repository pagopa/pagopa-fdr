package it.gov.pagopa.fdr.util.error.enums;

import it.gov.pagopa.fdr.util.constant.AppConstant;
import it.gov.pagopa.fdr.util.logging.AppMessageUtil;
import org.jboss.resteasy.reactive.RestResponse;

public enum AppErrorCodeMessageEnum {
  ERROR(
      "0500",
      "system.error",
      RestResponse.Status.INTERNAL_SERVER_ERROR,
      "An error occurred during computation. This could be caused by an applicative error and it is"
          + " probably required to open an issue."),

  // syntactic errors
  BAD_REQUEST(
      "1000",
      "bad.request",
      RestResponse.Status.BAD_REQUEST,
      "A generic 'Bad Request' error is occurred during request validation."),
  BAD_REQUEST_INPUT_JSON(
      "1001",
      "bad.request.inputJson",
      RestResponse.Status.BAD_REQUEST,
      "A generic error occurred during execution of request syntactic validation"),
  BAD_REQUEST_INPUT_JSON_INSTANT(
      "1002",
      "bad.request.inputJson.instant",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during execution of request syntactic validation, in particular regarding"
          + " the analysis of date values"),
  BAD_REQUEST_INPUT_JSON_ENUM(
      "1003",
      "bad.request.inputJson.enum",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during execution of request syntactic validation, in particular regarding"
          + " the analysis of enumerative values"),
  BAD_REQUEST_INPUT_JSON_DESERIALIZE_ERROR(
      "1004",
      "bad.request.inputJson.deserialize",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during execution of deserialization of request from a JSON string"),
  BAD_REQUEST_INPUT_JSON_NON_VALID_FORMAT(
      "1005",
      "bad.request.inputJson.notValidJsonFormat",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during execution of analysis of JSON request, in particular regarding its"
          + " format"),

  // semantic errors - cached configuration
  PSP_UNKNOWN(
      "2000",
      "pspId.unknown",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed PSP identifier. That PSP is not valid and maybe it"
          + " does not exists in current environment"),
  PSP_NOT_ENABLED(
      "2001",
      "pspId.notEnabled",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed PSP identifier. That PSP is not enabled and cannot be"
          + " used for elaboration"),
  BROKER_UNKNOWN(
      "2002",
      "brokerId.unknown",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed PSP Broker identifier. That PSP Broker is not valid"
          + " and maybe it does not exists in current environment"),
  BROKER_NOT_ENABLED(
      "2003",
      "brokerId.notEnabled",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed PSP Broker identifier. That PSP Broker is not enabled"
          + " and cannot be used for elaboration"),
  CHANNEL_UNKNOWN(
      "2004",
      "channelId.unknown",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed Channel identifier. That Channel is not valid and"
          + " maybe it does not exists in current environment"),
  CHANNEL_NOT_ENABLED(
      "2005",
      "channelId.notEnabled",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed Channel identifier. That Channel is not enabled and"
          + " cannot be used for elaboration"),
  CHANNEL_BROKER_WRONG_CONFIG(
      "2006",
      "channel.broker.wrongConfig",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed Channel identifier. That Channel is not correctly"
          + " configured for the passed PSP Broker and cannot be used for elaboration. In order to"
          + " be used, it is required to be configured by PagoPA operator"),
  CHANNEL_PSP_WRONG_CONFIG(
      "2007",
      "channel.psp.wrongConfig",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed Channel identifier. That Channel is not correctly"
          + " configured for the passed PSP and cannot be used for elaboration. In order to be"
          + " used, it is required to be configured by PagoPA operator"),
  EC_UNKNOWN(
      "2008",
      "ecId.unknown",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed Creditor Institution identifier. That Creditor"
          + " Institution is not valid and maybe it does not exists in current environment"),
  EC_NOT_ENABLED(
      "2009",
      "ecId.notEnabled",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred regarding the passed Creditor Institution identifier. That Creditor"
          + " Institution is not enabled and cannot be used for elaboration"),

  // semantic checks - prepublish-related checks
  REPORTING_FLOW_NOT_FOUND(
      "3001",
      "fdr.notFound",
      RestResponse.Status.NOT_FOUND,
      "An error occurred during the search of reporting flow. The needed flow does not exists in"
          + " current environment"),
  REPORTING_FLOW_ALREADY_EXIST(
      "3002",
      "fdr.alreadyExist",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during the search of reporting flow. The flow being created already exists"
          + " in CREATED/INSERTED status and cannot be overridden. The existing flow must be"
          + " deleted in order to create another flow with the same name."),
  REPORTING_FLOW_WRONG_ACTION(
      "3003",
      "fdr.wrongAction",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during the search of reporting flow. The flow being updated cannot be"
          + " handled with some kind of action (for example, trying to publish a flow in CREATED"
          + " status)"),
  REPORTING_FLOW_PSP_ID_NOT_MATCH(
      "3004",
      "fdr.pspId.notMatch",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis. The value of PSP identifier set on query parameter"
          + " is not equals to the one defined in the request. So, it is required to update one of"
          + " them in order to proceed."),
  REPORTING_FLOW_PAYMENT_SAME_INDEX_IN_SAME_REQUEST(
      "3005",
      "fdr.sameIndexInSameRequest",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis regarding the included payments. In particular, there"
          + " are at least one index that is duplicated in the request and it is required to delete"
          + " them in order to proceed."),
  REPORTING_FLOW_PAYMENT_DUPLICATE_INDEX(
      "3006",
      "fdr.duplicateIndex",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis regarding the included payments. In particular, there"
          + " are at least one index that is duplicated in the whole flow and it is required to"
          + " exclude them in the request in order to proceed."),
  REPORTING_FLOW_PAYMENT_NO_MATCH_INDEX(
      "3007",
      "fdr.noMatchIndex",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis regarding the included payments. In particular, there"
          + " are at least one index that does not exists in the whole flow it is required to"
          + " delete them from request in order to proceed."),
  REPORTING_FLOW_NAME_DATE_WRONG_FORMAT(
      "3008",
      "fdr.name-date.wrongFormat",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during execution of analysis on flow identifier. In particular, the date"
          + " included in the identifier is not correctly formatted and it is not compliant with"
          + " SANP specification"),
  REPORTING_FLOW_NAME_PSP_WRONG_FORMAT(
      "3009",
      "fdr.name-psp.wrongFormat",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during execution of analysis on flow identifier. In particular, the PSP"
          + " Identifier included in the flow identifier is not correct and it is not compliant"
          + " with SANP specification"),
  REPORTING_FLOW_NAME_NOT_MATCH(
      "3010",
      "fdr.name.notMatch",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis. The value of flow identifier set on query parameter"
          + " is not equals to the one defined in the request. So, it is required to update one of"
          + " them in order to proceed."),

  REPORTING_FLOW_DATE_NOT_COMPLIANT(
      "3011",
      "fdr.date.wrongFormat",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis. The value of flow date"
          + " is not compliant with the required values. The date must "
          + " come after the one in the last revision."),

  // semantic checks - publish-related checks
  REPORTING_FLOW_WRONG_TOT_PAYMENT(
      "4001",
      "fdr.wrongTotPayment",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis before publish operation. In particular, the number"
          + " of total payments added by previous steps are not equals to the quantity pre-defined"
          + " in flow during creation process."),
  REPORTING_FLOW_WRONG_SUM_PAYMENT(
      "4002",
      "fdr.wrongSumPayment",
      RestResponse.Status.BAD_REQUEST,
      "An error occurred during flow analysis before publish operation. In particular, the number"
          + " of total amout for payments added by previous steps are not equals to the quantity"
          + " pre-defined in flow during creation process."),
  ;
  // other checks
  // OTHER_ERROR("5000", "fdr.fileUtilsConversionError", Status.INTERNAL_SERVER_ERROR, "-");

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
