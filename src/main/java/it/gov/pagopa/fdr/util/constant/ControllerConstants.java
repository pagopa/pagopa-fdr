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
  public static final String PARAMETER_PUBLISHED_GREATER_THAN = "publishedGt";
  public static final String PARAMETER_REVISION = "revision";

  public static final String URL_API_GET_SINGLE_FLOW =
      "/{"
          + ControllerConstants.PARAMETER_FDR
          + "}/revisions/{"
          + ControllerConstants.PARAMETER_REVISION
          + "}/psps/{"
          + ControllerConstants.PARAMETER_PSP
          + "}";

  public static final String URL_API_GET_FLOW_PAYMENTS =
      "/{"
          + ControllerConstants.PARAMETER_FDR
          + "}/revisions/{"
          + ControllerConstants.PARAMETER_REVISION
          + "}/psps/{"
          + ControllerConstants.PARAMETER_PSP
          + "}/payments";

  public static final String URL_CONTROLLER_ORGANIZATIONS =
      "/organizations/{" + ControllerConstants.PARAMETER_ORGANIZATION + "}/fdrs";

  public static final String URL_CONTROLLER_INTERNAL_ORGANIZATIONS =
      "/internal" + URL_CONTROLLER_ORGANIZATIONS;

  private ControllerConstants() {}
}
