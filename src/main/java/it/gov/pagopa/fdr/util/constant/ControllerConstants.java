package it.gov.pagopa.fdr.util.constant;

public class ControllerConstants {

  public static final String PARAMETER_CREATED_FROM = "createdFrom";
  public static final String PARAMETER_CREATED_TO = "createdTo";
  public static final String PARAMETER_FDR = "fdr";
  public static final String PARAMETER_IUR = "iur";
  public static final String PARAMETER_IUV = "iuv";
  public static final String PARAMETER_ORGANIZATION = "organizationId";
  public static final String PARAMETER_PAGE_INDEX = "page";
  public static final String PARAMETER_PAGE_INDEX_DEFAULT = "1";
  public static final String PARAMETER_PAGE_SIZE = "size";
  public static final String PARAMETER_PAGE_SIZE_DEFAULT = "1000";
  public static final String PARAMETER_PSP = "pspId";
  public static final String PARAMETER_CREATED_GREATER_THAN = "createdGt";
  public static final String PARAMETER_PUBLISHED_GREATER_THAN = "publishedGt";
  public static final String PARAMETER_REVISION = "revision";

  public static final String URL_PARAMETER_FDR = "fdrs/{" + ControllerConstants.PARAMETER_FDR + "}";
  public static final String URL_PARAMETER_ORGANIZATION =
      "organizations/{" + ControllerConstants.PARAMETER_ORGANIZATION + "}";
  public static final String URL_PARAMETER_REVISION =
      "revisions/{" + ControllerConstants.PARAMETER_REVISION + "}";
  public static final String URL_PARAMETER_PSP = "psps/{" + ControllerConstants.PARAMETER_PSP + "}";

  public static final String URL_API_GET_SINGLE_FLOW =
      "/{"
          + ControllerConstants.PARAMETER_FDR
          + "}/"
          + ControllerConstants.URL_PARAMETER_REVISION
          + "/"
          + ControllerConstants.URL_PARAMETER_PSP;

  public static final String URL_API_GET_FLOW_PAYMENTS =
      "/{"
          + ControllerConstants.PARAMETER_FDR
          + "}/"
          + ControllerConstants.URL_PARAMETER_REVISION
          + "/"
          + ControllerConstants.URL_PARAMETER_PSP
          + "/payments";

  public static final String URL_API_CREATE_EMPTY_FLOW = ControllerConstants.URL_PARAMETER_FDR;

  public static final String URL_API_DELETE_FLOW = ControllerConstants.URL_PARAMETER_FDR;

  public static final String URL_API_GET_ALL_NOT_PUBLISHED_FLOWS = "/created";

  public static final String URL_API_GET_ALL_PUBLISHED_FLOWS = "/published";

  public static final String URL_API_GET_SINGLE_NOT_PUBLISHED_FLOW =
      ControllerConstants.URL_API_GET_ALL_NOT_PUBLISHED_FLOWS
          + "/"
          + ControllerConstants.URL_PARAMETER_FDR
          + "/"
          + ControllerConstants.URL_PARAMETER_ORGANIZATION;

  public static final String URL_API_GET_SINGLE_PUBLISHED_FLOW =
      ControllerConstants.URL_API_GET_ALL_PUBLISHED_FLOWS
          + "/"
          + ControllerConstants.URL_PARAMETER_FDR
          + "/"
          + ControllerConstants.URL_PARAMETER_REVISION
          + "/"
          + ControllerConstants.URL_PARAMETER_ORGANIZATION;

  public static final String URL_API_GET_PAYMENTS_FOR_NOT_PUBLISHED_FLOW =
      ControllerConstants.URL_API_GET_SINGLE_NOT_PUBLISHED_FLOW + "/payments";

  public static final String URL_API_GET_PAYMENTS_FOR_PUBLISHED_FLOW =
      ControllerConstants.URL_API_GET_SINGLE_PUBLISHED_FLOW + "/payments";
  public static final String URL_API_ADD_PAYMENT_IN_FLOW =
      "/" + ControllerConstants.URL_PARAMETER_FDR + "/payments/add";

  public static final String URL_API_DELETE_PAYMENT_IN_FLOW =
      "/" + ControllerConstants.URL_PARAMETER_FDR + "/payments/del";

  public static final String URL_API_PUBLISH_FLOW =
      "/" + ControllerConstants.URL_PARAMETER_FDR + "/publish";

  public static final String URL_CONTROLLER_ORGANIZATIONS =
      "/" + ControllerConstants.URL_PARAMETER_ORGANIZATION + "/fdrs";

  public static final String URL_CONTROLLER_INTERNAL_ORGANIZATIONS =
      "/internal" + ControllerConstants.URL_CONTROLLER_ORGANIZATIONS;

  public static final String URL_CONTROLLER_PSPS = "/" + ControllerConstants.URL_PARAMETER_PSP;

  public static final String URL_CONTROLLER_INTERNAL_PSPS = "/internal" + URL_CONTROLLER_PSPS;

  private ControllerConstants() {}
}
