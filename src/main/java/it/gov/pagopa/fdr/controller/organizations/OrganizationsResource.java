package it.gov.pagopa.fdr.controller.organizations;

import it.gov.pagopa.fdr.Config;
import it.gov.pagopa.fdr.controller.model.flow.FlowResponse;
import it.gov.pagopa.fdr.controller.model.flow.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.payment.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.controller.organizations.mapper.OrganizationsResourceServiceMapper;
import it.gov.pagopa.fdr.controller.organizations.validation.InternalOrganizationsValidationService;
import it.gov.pagopa.fdr.controller.organizations.validation.OrganizationsValidationService;
import it.gov.pagopa.fdr.service.organizations.OrganizationsService;
import it.gov.pagopa.fdr.service.re.model.FdrActionEnum;
import it.gov.pagopa.fdr.util.AppConstant;
import it.gov.pagopa.fdr.util.Re;
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
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Tag(name = "Organizations", description = "Organizations operations")
@Path("/organizations/{" + AppConstant.ORGANIZATION + "}/fdrs")
@Consumes("application/json")
@Produces("application/json")
public class OrganizationsResource extends BaseOrganizationsResource {

  protected OrganizationsResource(
      Config config,
      Logger log,
      OrganizationsValidationService validator,
      InternalOrganizationsValidationService internalValidator,
      OrganizationsResourceServiceMapper mapper,
      OrganizationsService service) {
    super(config, log, validator, internalValidator, mapper, service);
  }

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
                    schema = @Schema(implementation = PaginatedFlowsResponse.class)))
      })
  @GET
  @Re(action = FdrActionEnum.GET_ALL_FDR)
  public PaginatedFlowsResponse getAllPublished(
      @PathParam(AppConstant.ORGANIZATION) @Pattern(regexp = "^(.{1,35})$") String organizationId,
      @QueryParam(AppConstant.PSP) @Pattern(regexp = "^(.{1,35})$") String idPsp,
      @QueryParam(AppConstant.PUBLISHED_GREATER_THAN) Instant publishedGt,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    return baseGetAll(organizationId, idPsp, publishedGt, pageNumber, pageSize, false);
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
                    schema = @Schema(implementation = FlowResponse.class)))
      })
  @GET
  @Path(
      "/{"
          + AppConstant.FDR
          + "}/revisions/{"
          + AppConstant.REVISION
          + "}/psps/{"
          + AppConstant.PSP
          + "}")
  @Re(action = FdrActionEnum.GET_FDR)
  public FlowResponse get(
      @PathParam(AppConstant.ORGANIZATION) String organizationId,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.REVISION) Long rev,
      @PathParam(AppConstant.PSP) String psp) {
    return baseGet(organizationId, fdr, rev, psp, true);
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
                    schema = @Schema(implementation = PaginatedPaymentsResponse.class)))
      })
  @GET
  @Path(
      "/{"
          + AppConstant.FDR
          + "}/revisions/{"
          + AppConstant.REVISION
          + "}/psps/{"
          + AppConstant.PSP
          + "}/payments")
  @Re(action = FdrActionEnum.GET_FDR_PAYMENT)
  public PaginatedPaymentsResponse getPayment(
      @PathParam(AppConstant.ORGANIZATION) String organizationId,
      @PathParam(AppConstant.FDR) String fdr,
      @PathParam(AppConstant.REVISION) Long rev,
      @PathParam(AppConstant.PSP) String psp,
      @QueryParam(AppConstant.PAGE) @DefaultValue(AppConstant.PAGE_DEAFULT) @Min(value = 1)
          long pageNumber,
      @QueryParam(AppConstant.SIZE) @DefaultValue(AppConstant.SIZE_DEFAULT) @Min(value = 1)
          long pageSize) {
    return baseGetFdrPayment(organizationId, fdr, rev, psp, pageNumber, pageSize, false);
  }
}
