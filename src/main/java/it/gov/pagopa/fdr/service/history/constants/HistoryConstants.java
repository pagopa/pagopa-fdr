package it.gov.pagopa.fdr.service.history.constants;

public class HistoryConstants {
  private HistoryConstants() {
    throw new IllegalStateException("Utility Class");
  }

  public static final String FDR_PUBLISH_ID = "id";
  public static final String FDR_PUBLISH_REVISION = "revision";
  public static final String FDR_PUBLISH_CREATED = "created";
  public static final String FDR_PUBLISH_UPDATED = "updated";
  public static final String FDR_PUBLISH_PUBLISHED = "published";
  public static final String FDR_PUBLISH_FDR = "fdr";
  public static final String FDR_PUBLISH_FDR_DATE = "fdr_date";
  public static final String FDR_PUBLISH_FDR_REF_JSON_CONTAINER_NAME = "jsonref_container_name";
  public static final String FDR_PUBLISH_FDR_REF_JSON_FILE_LENGTH = "jsonref_file_length";
  public static final String FDR_PUBLISH_FDR_REF_JSON_FILE_NAME = "jsonref_file_name";
  public static final String FDR_PUBLISH_FDR_REF_JSON_STORAGE_ACCOUNT = "jsonref_storage_account";
  public static final String FDR_PUBLISH_FDR_REF_JSON_JSON_SCHEMA_VERSION =
      "jsonref_json_schema_version";
  public static final String FDR_PUBLISH_SENDER_TYPE = "sender_type";
  public static final String FDR_PUBLISH_SENDER_ID = "sender_id";
  public static final String FDR_PUBLISH_SENDER_PSP_ID = "sender_psp_id";
  public static final String FDR_PUBLISH_SENDER_PSP_NAME = "sender_psp_name";
  public static final String FDR_PUBLISH_SENDER_PSP_BROKER_ID = "sender_psp_broker_id";
  public static final String FDR_PUBLISH_SENDER_CHANNEL_ID = "sender_channel_id";
  public static final String FDR_PUBLISH_SENDER_PASSWORD = "sender_password";
  public static final String FDR_PUBLISH_RECEIVER_ID = "receiver_id";
  public static final String FDR_PUBLISH_RECEIVER_ORGANIZATION_ID = "receiver_organization_id";
  public static final String FDR_PUBLISH_RECEIVER_ORGANIZATION_NAME = "receiver_organization_name";
  public static final String FDR_PUBLISH_REGULATION = "regulation";
  public static final String FDR_PUBLISH_REGULATION_DATE = "regulation_date";
  public static final String FDR_PUBLISH_BIC_CODE_POURING_BANK = "bic_code_pouring_bank";
  public static final String FDR_PUBLISH_STATUS = "status";
  public static final String FDR_PUBLISH_COMPUTED_TOT_PAYMENTS = "computed_tot_payments";
  public static final String FDR_PUBLISH_COMPUTED_SUM_PAYMENTS = "computed_sum_payments";
  public static final String FDR_PUBLISH_TOT_PAYMENTS = "tot_payments";
  public static final String FDR_PUBLISH_SUM_PAYMENTS = "sum_payments";
  public static final String FDR_PAYMENT_PUBLISH_ID = "id";
  public static final String FDR_PAYMENT_PUBLISH_REVISION = "revision";
  public static final String FDR_PAYMENT_PUBLISH_CREATED = "created";
  public static final String FDR_PAYMENT_PUBLISH_UPDATED = "updated";
  public static final String FDR_PAYMENT_PUBLISH_IUV = "iuv";
  public static final String FDR_PAYMENT_PUBLISH_IUR = "iur";
  public static final String FDR_PAYMENT_PUBLISH_INDEX = "index";
  public static final String FDR_PAYMENT_PUBLISH_PAY = "pay";
  public static final String FDR_PAYMENT_PUBLISH_PAY_STATUS = "pay_status";
  public static final String FDR_PAYMENT_PUBLISH_PAY_DATE = "pay_date";
  public static final String FDR_PAYMENT_PUBLISH_REF_FDR_ID = "ref_fdr_id";
  public static final String FDR_PAYMENT_PUBLISH_REF_FDR = "ref_fdr";
  public static final String FDR_PAYMENT_PUBLISH_REF_FDR_SENDER_PSP_ID = "ref_fdr_sender_psp_id";
  public static final String FDR_PAYMENT_PUBLISH_REF_FDR_REVISION = "ref_fdr_revision";
  public static final String FDR_PAYMENT_PUBLISH_REF_FDR_RECEIVER_ORGANIZATION_ID =
      "ref_fdr_receiver_organization_id";
}
