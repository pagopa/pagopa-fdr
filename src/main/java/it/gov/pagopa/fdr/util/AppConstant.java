package it.gov.pagopa.fdr.util;

import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import java.util.ArrayList;
import java.util.List;

public class AppConstant {

  public static final String SERVICE_CODE_APP = "FDR";

  public static final String PSP = "psp";
  public static final String EC = "ec";
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

  private static List<FlowActionEnum> flowActionExcludeToSendEvent = new ArrayList<>();

  static {
    flowActionExcludeToSendEvent.add(FlowActionEnum.INFO);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_CREATE_FLOW);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_DELETE_FLOW);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_ADD_PAYMENT);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_DELETE_PAYMENT);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_PUBLISH);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_GET_ALL_FDR);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_GET_FDR);
    //    flowActionExcludeToSendEvent.add(FlowActionEnum.INTERNAL_GET_FDR_PAYMENT);
  }
  ;

  public static boolean sendReEvent(FlowActionEnum flowActionEnum) {
    return !flowActionExcludeToSendEvent.contains(flowActionEnum);
  }
}
