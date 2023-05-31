package it.gov.pagopa.fdr.repository.fdr;

public class QueryConstants {

  protected static final String BY_FLOWNAME_AND_PSPID =
      "reporting_flow_name = :flowName and sender.psp_id = :pspId";

  protected static final String BY_REFFLOWNAME_AND_INDEXES =
      "ref_fdr_reporting_flow_name = :flowName and index in :indexes";

  protected static final String BY_REFFLOWNAME_AND_REFPSPID =
      "ref_fdr_reporting_flow_name = :flowName and ref_fdr_reporting_sender_psp_id = :pspId";

  protected static final String BY_REFFLOWNAME = "ref_fdr_reporting_flow_name = :flowName";

  protected static final String BY_ECID_AND_PSPID =
      "receiver.ec_id = :ecId and sender.psp_id = :pspId";

  protected static final String BY_ECID = "receiver.ec_id = :ecId";

  protected static final String BY_INTERNAL_NDP_READ = "receiver.internal_ndp_read = :internalRead";

  private QueryConstants() {
    throw new IllegalStateException("Constants class");
  }
}
