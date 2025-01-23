package it.gov.pagopa.fdr.util.constant;

import it.gov.pagopa.fdr.service.model.re.FdrActionEnum;
import java.util.ArrayList;
import java.util.List;

public class AppConstant {

  public static final String SERVICE_CODE_APP = "FDR";

  public static final String REQUEST = "REQ";
  public static final String RESPONSE = "RES";
  public static final String OK = "OK";
  public static final String KO = "KO";
  public static final String PSP = "pspId";
  public static final String PUBLISHED_GREATER_THAN = "publishedGt";
  public static final String CREATED_GREATER_THAN = "createdGt";

  public static final String ORGANIZATION = "organizationId";

  public static final String FDR = "fdr";
  public static final String REVISION = "revision";
  public static final String PAGE = "page";
  public static final String PAGE_DEAFULT = "1";
  public static final String SIZE = "size";
  public static final String SIZE_DEFAULT = "1000";
  public static final int MAX_PAYMENT = 1000;

  private AppConstant() {
    throw new IllegalStateException("Constants class");
  }

  private static final List<FdrActionEnum> fdrActionExcludeToSendEvent = new ArrayList<>();

  static {
    fdrActionExcludeToSendEvent.add(FdrActionEnum.INFO);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_CREATE_FLOW);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_DELETE_FLOW);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_ADD_PAYMENT);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_DELETE_PAYMENT);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_PUBLISH);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_GET_ALL_FDR);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_GET_FDR);
    //    fdrActionExcludeToSendEvent.add(FdrActionEnum.INTERNAL_GET_FDR_PAYMENT);
  }

  public static boolean sendReEvent(FdrActionEnum fdrActionEnum) {
    return !fdrActionExcludeToSendEvent.contains(fdrActionEnum);
  }
}
