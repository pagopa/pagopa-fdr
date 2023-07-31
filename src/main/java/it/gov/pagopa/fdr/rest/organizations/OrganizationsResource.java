package it.gov.pagopa.fdr.rest.organizations;

import static it.gov.pagopa.fdr.util.MDCKeys.ACTION;
import static it.gov.pagopa.fdr.util.MDCKeys.FDR;
import static it.gov.pagopa.fdr.util.MDCKeys.ORGANIZATION_ID;
import static it.gov.pagopa.fdr.util.MDCKeys.PSP_ID;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.rest.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.rest.organizations.response.GetAllResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetPaymentResponse;
import it.gov.pagopa.fdr.rest.organizations.response.GetResponse;
import it.gov.pagopa.fdr.rest.organizations.validation.OrganizationsValidationService;
import it.gov.pagopa.fdr.service.dto.FdrAllDto;
import it.gov.pagopa.fdr.service.dto.FdrGetDto;
import it.gov.pagopa.fdr.service.dto.FdrGetPaymentDto;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
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

@Tag(name = "Organizations", description = "Organizations operations")
@Path("/organizations/{" + AppConstant.ORGANIZATION + "}/fdrs")
@Consumes("application/json")
@Produces("application/json")
public class OrganizationsResource {

  @Inject Config config;
  @Inject Logger log;

  @Inject OrganizationsValidationService validator;

  @Inject OrganizationsResourceServiceMapper mapper;

  @Inject OrganizationsService service;

  @Operation(
      operationId = "getAllPublished",
      summary = "Get all fdr published",
      description = "Get all fdr published")
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
  @Re(action = FdrActionEnum.GET_ALL_FDR)
  public GetAllResponse getAllPublished(
      @PathParam(AppConstant.ORGANIZATION) @Pattern(regexp = "^(.{1,35})$") String organizationId,
      @QueryParam(AppConstant.PSP) @Pattern(regexp = "^(.{1,35})$") String idPsp,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    String action = MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    if (null != idPsp && !idPsp.isBlank()) {
      MDC.put(PSP_ID, idPsp);
    }

    log.infof(
        AppMessageUtil.logProcess("%s by ec:[%s] with psp:[%s] - page:[%s], pageSize:[%s]"),
        action,
        organizationId,
        idPsp,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();
    // validation
    validator.validateGetAllByEc(action, organizationId, idPsp, configData);

    // get from db
    FdrAllDto fdrAllDto = service.findByIdEc(action, organizationId, idPsp, pageNumber, pageSize);

    return mapper.toGetAllResponse(fdrAllDto);
  }

  @Operation(operationId = "get", summary = "Get fdr", description = "Get fdr")
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
                    schema = @Schema(implementation = GetResponse.class)))
      })
  @GET
  @Path("/{" + AppConstant.FDR + "}/psps/{" + AppConstant.PSP + "}")
  @Re(action = FdrActionEnum.GET_FDR)
  public GetResponse get(
      @PathParam(AppConstant.ORGANIZATION) String organizationId,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.PSP) String psp) {
    String action = MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess("%s by ec:[%s] with fdr=[%s], psp=[%s]"),
        action,
        fdr,
        organizationId,
        psp);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateGet(action, fdr, organizationId, psp, configData);

    // get from db
    FdrGetDto fdrGetDto = service.findByReportingFlowName(action, fdr, psp);

    return mapper.toGetIdResponse(fdrGetDto);
  }

  @Operation(
      operationId = "getPayment",
      summary = "Get payments of fdr",
      description = "Get payments of fdr")
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
  @Re(action = FdrActionEnum.GET_FDR_PAYMENT)
  public GetPaymentResponse getPayment(
      @PathParam(AppConstant.ORGANIZATION) String organizationId,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.PSP) String psp,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    String action = MDC.get(ACTION);
    MDC.put(ORGANIZATION_ID, organizationId);
    MDC.put(FDR, fdr);
    MDC.put(PSP_ID, psp);

    log.infof(
        AppMessageUtil.logProcess(
            "%s by ec:[%s] with fdr:[%s], psp:[%s] - page:[%s], pageSize:[%s]"),
        action,
        fdr,
        organizationId,
        psp,
        pageNumber,
        pageSize);

    ConfigDataV1 configData = config.getClonedCache();

    // validation
    validator.validateGetPayment(action, fdr, organizationId, psp, configData);

    // get from db
    FdrGetPaymentDto fdrGetPaymentDto =
        service.findPaymentByReportingFlowName(action, fdr, psp, pageNumber, pageSize);

    return mapper.toGetPaymentResponse(fdrGetPaymentDto);
  }
}
