package it.gov.pagopa.fdr.rest.organizations;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.EC_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.FLOW_NAME;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetIdResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.validation.OrganizationsValidationService;
import it.gov.pagopa.fdr.service.dto.ReportingFlowByIdEcDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetDto;
import it.gov.pagopa.fdr.service.dto.ReportingFlowGetPaymentDto;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import it.gov.pagopa.fdr.service.re.model.FlowActionEnum;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import it.gov.pagopa.fdr.util.Re;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.slf4j.MDC;

@Tag(name = "Organizations", description = "Get reporting flow operations")
@Path("/organizations/{" + AppConstant.EC + "}/flows")
@Consumes("application/json")
@Produces("application/json")
public class OrganizationsResource {

  @Inject Config config;
  @Inject Logger log;

  @Inject OrganizationsValidationService validator;

  @Inject OrganizationsResourceServiceMapper mapper;

  @Inject OrganizationsService service;

  @Operation(
      operationId = "getAllPublishFdr",
      summary = "Get all published reporting flow",
      description = "Get all published reporting flow by ec and idPsp(optional param)")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetAllResponse.class)))
      })
  @GET
  @Re(flowName = FlowActionEnum.GET_ALL_FDR)
  public GetAllResponse getAllPublishFdr(
      @PathParam(AppConstant.EC) @Pattern(regexp = "^(.{1,35})$") String ec,
      @QueryParam(AppConstant.PSP) @Pattern(regexp = "^(.{1,35})$") String idPsp,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    String action = MDC.get(ACTION);
    MDC.put(EC_ID, ec);
    if (null != idPsp && !idPsp.isBlank()) {
      MDC.put(PSP_ID, idPsp);
    }

    log.infof(
        AppMessageUtil.logProcess("%s by ec:[%s] with psp:[%s] - page:[%s], pageSize:[%s]"),
        action,
        ec,
        idPsp,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateGetAllByEc(action, ec, idPsp, configData);

    // get from db
    ReportingFlowByIdEcDto reportingFlowByIdEcDto =
        service.findByIdEc(action, ec, idPsp, pageNumber, pageSize);

    return mapper.toGetAllResponse(reportingFlowByIdEcDto);
  }

  @Operation(
      operationId = "getFdr",
      summary = "Get reporting flow",
      description = "Get reporting flow by id but not payments")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetIdResponse.class)))
      })
  @GET
  @Path("/{" + AppConstant.FDR + "}/psps/{" + AppConstant.PSP + "}")
  @Re(flowName = FlowActionEnum.GET_FDR)
  public GetIdResponse getFdr(
      @PathParam(AppConstant.EC) String ec,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.PSP) String psp) {
    String action = MDC.get(ACTION);
    MDC.put(EC_ID, ec);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by ec:[%s] with fdr=[%s], psp=[%s]"), action, fdr, ec, psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateGet(action, fdr, ec, psp, configData);

    // get from db
    ReportingFlowGetDto reportingFlowGetDto = service.findByReportingFlowName(action, fdr, psp);

    return mapper.toGetIdResponse(reportingFlowGetDto);
  }

  @Operation(
      operationId = "getFdrPayment",
      summary = "Get payments of reporting flow",
      description = "Get only payments of reporting flow by id paginated")
  @APIResponses(
      value = {
        @APIResponse(ref = "#/components/responses/InternalServerError"),
        @APIResponse(ref = "#/components/responses/AppException400"),
        @APIResponse(ref = "#/components/responses/AppException404"),
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetPaymentResponse.class)))
      })
  @GET
  @Path("/{" + AppConstant.FDR + "}/psps/{" + AppConstant.PSP + "}/payments")
  @Re(flowName = FlowActionEnum.GET_FDR_PAYMENT)
  public GetPaymentResponse getFdrPayment(
      @PathParam(AppConstant.EC) String ec,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.PSP) String psp,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    String action = MDC.get(ACTION);
    MDC.put(EC_ID, ec);
    MDC.put(FLOW_NAME, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess(
            "%s by ec:[%s] with fdr:[%s], psp:[%s] - page:[%s], pageSize:[%s]"),
        action,
        fdr,
        ec,
        psp,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateGetPayment(action, fdr, ec, psp, configData);

    // get from db
    ReportingFlowGetPaymentDto reportingFlowGetPaymentDto =
        service.findPaymentByReportingFlowName(action, fdr, psp, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(reportingFlowGetPaymentDto);
  }
}
