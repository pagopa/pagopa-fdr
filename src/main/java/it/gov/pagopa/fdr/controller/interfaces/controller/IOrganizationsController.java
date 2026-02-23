package it.gov.pagopa.fdr.controller.interfaces.controller;

import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.SingleFlowResponse;
import it.gov.pagopa.fdr.controller.model.payment.response.PaginatedPaymentsResponse;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.openapi.APIAppErrorMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISecurityMode;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISynchronism;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.ReadWrite;
import it.gov.pagopa.fdr.util.validator.PastDateLimit;
import jakarta.validation.constraints.Max;
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
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(ControllerConstants.URL_CONTROLLER_ORGANIZATIONS)
@Consumes("application/json")
@Produces("application/json")
@Tag(
    name = "Organizations",
    description = "APIs for creditor institution, used for inspection of generated flows")
public interface IOrganizationsController {

  @GET
  @Operation(
      operationId = "IOrganizationsController_getAllPublishedFlows",
      summary = "Get all published flow related to creditor institution",
      description =
"""
This API permits to search all published flows for a specific creditor institution,
formatted in a paginated view. The search can be enhanced including the PSP identifier
in order to filter only the flows for certain PSP. The only flows retrieved are the latest
revision, as same as "nodoChiediElencoFlussiRendicontazione" primitive does.<br>
Before executing the query, the search filters are validated against entities configured for
<i>Nodo dei Pagamenti</i> environment, in particular on PSP (if that search filter is defined).<br>
The result of the query is paginated and contains all the metadata needed for pagination purposes.<br>
""")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PaginatedFlowsResponse.class))),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_BADREQUEST_EXAMPLE)),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_NOTFOUND_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_INTERNALSERVERERROR_EXAMPLE))
      })
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.EC_UNKNOWN,
        AppErrorCodeMessageEnum.EC_NOT_ENABLED
      })
  PaginatedFlowsResponse getAllPublishedFlows(
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION)
          @Pattern(regexp = "^(.{1,35})$")
          @Parameter(
              description = "The creditor institution identifier, used as a search filter",
              example = "15376371009")
          String organizationId,
      @Parameter(
              description = "The PSP identifier, used as a search filter",
              example = "88888888888")
          @QueryParam(ControllerConstants.PARAMETER_PSP)
          @Pattern(regexp = "^(.{1,35})$")
          String pspId,
      @Parameter(
              description =
                  "A date to be used as a lower limit search on publication date. In format"
                      + " ISO-8601 (yyyy-MM-dd'T'HH:mm:ss). If omitted, the server uses a "
                      + "dynamic default equal to the start of the previous calendar month at 00:00 UTC",
              example = "2025-01-01T12:00:00Z")
          @PastDateLimit(value = 1, unit = ChronoUnit.MONTHS)
          @QueryParam(ControllerConstants.PARAMETER_PUBLISHED_GREATER_THAN)
          Optional<Instant> publishedGt,
      @Parameter(
              description =
                  "A date to be used as a lower limit search on flow date. In format"
                      + " ISO-8601 (yyyy-MM-dd'T'HH:mm:ss). If omitted, the server uses"
                      + " a dynamic default equal to the start of the previous calendar month at 00:00 UTC",
              example = "2025-01-01T12:00:00Z")
          @PastDateLimit(value = 1, unit = ChronoUnit.MONTHS)
          @QueryParam(ControllerConstants.PARAMETER_FLOW_DATE_GREATER_THAN)
          Optional<Instant> flowDate,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          @Parameter(description = "The index of the page to be shown in the result", example = "1")
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          @Max(value = 1000)
          @Parameter(
              description = "The number of the elements of the page to be shown in the result",
              example = "50")
          long pageSize);

  @GET
  @Path(ControllerConstants.URL_API_GET_SINGLE_FLOW)
  @Operation(
      operationId = "IOrganizationsController_getSinglePublishedFlow",
      summary =
          "Get single published flow related to creditor institution, searching by name and"
              + " revision",
      description =
"""
This API permits to search a single published flows for a specific creditor institution.
In order to do so, it is required to add the following search filters:
 - Creditor institution identifier: for filtering by specific organization
 - PSP identifier: for filtering by flow-related PSP
 - Flow name: for filtering by specific instance of the flow
 - Revision: for filtering by flow revision

Before executing the query, the search filters are validated against entities configured for
<i>Nodo dei Pagamenti</i> environment, in particular on creditor institution and PSP. Also,
the name of the flow is validated against a specific standard format.<br>
""")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SingleFlowResponse.class))),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_BADREQUEST_EXAMPLE)),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_NOTFOUND_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_INTERNALSERVERERROR_EXAMPLE))
      })
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.EC_UNKNOWN,
        AppErrorCodeMessageEnum.EC_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND
      })
  SingleFlowResponse getSinglePublishedFlow(
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION)
          @Parameter(
              description = "The creditor institution identifier, used as a search filter",
              example = "15376371009")
          String organizationId,
      @PathParam(ControllerConstants.PARAMETER_FDR)
          @Parameter(
              description = "The flow name, used as a search filter",
              example = "2025-01-0188888888888-0001")
          String flowName,
      @PathParam(ControllerConstants.PARAMETER_REVISION)
          @Min(value = 1)
          @Parameter(description = "The specific revision of the flow needed", example = "1")
          Long revision,
      @PathParam(ControllerConstants.PARAMETER_PSP)
          @Parameter(
              description = "The PSP identifier, used as a search filter",
              example = "88888888888")
          String psp);

  @GET
  @Path(ControllerConstants.URL_API_GET_FLOW_PAYMENTS)
  @Operation(
      operationId = "IOrganizationsController_getPaymentsFromPublishedFlow",
      summary =
          "Get all payments of single published flow related to creditor institution, searching by"
              + " name and revision",
      description =
"""
This API permits to search all the payments of a single published flow for a specific creditor institution,
formatted in a paginated view. In order to do so, it is required to add the following search filters:
 - Creditor institution identifier: for filtering by specific organization
 - PSP identifier: for filtering by flow-related PSP
 - Flow name: for filtering by specific instance of the flow
 - Revision: for filtering by flow revision

Before executing the query, the search filters are validated against entities configured for
<i>Nodo dei Pagamenti</i> environment, in particular on creditor institution and PSP. Also,
the name of the flow is validated against a specific standard format.<br>
The result of the query is paginated and contains all the metadata needed for pagination purposes.<br>
""")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PaginatedPaymentsResponse.class))),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_BADREQUEST_EXAMPLE)),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_NOTFOUND_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = ControllerConstants.OPENAPI_INTERNALSERVERERROR_EXAMPLE))
      })
  @APITableMetadata(
      internal = false,
      synchronism = APISynchronism.SYNC,
      authorization = APISecurityMode.AUTHORIZER,
      authentication = APISecurityMode.APIKEY,
      readWriteIntense = ReadWrite.READ,
      cacheable = true)
  @APIAppErrorMetadata(
      errors = {
        AppErrorCodeMessageEnum.PSP_UNKNOWN,
        AppErrorCodeMessageEnum.PSP_NOT_ENABLED,
        AppErrorCodeMessageEnum.EC_UNKNOWN,
        AppErrorCodeMessageEnum.EC_NOT_ENABLED,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT,
        AppErrorCodeMessageEnum.REPORTING_FLOW_NOT_FOUND
      })
  PaginatedPaymentsResponse getPaymentsFromPublishedFlow(
      @PathParam(ControllerConstants.PARAMETER_ORGANIZATION)
          @Parameter(
              description = "The creditor institution identifier, used as a search filter",
              example = "15376371009")
          String organizationId,
      @PathParam(ControllerConstants.PARAMETER_FDR)
          @Parameter(
              description = "The flow name, used as a search filter",
              example = "2025-01-0188888888888-0001")
          String flowName,
      @PathParam(ControllerConstants.PARAMETER_REVISION)
          @Min(value = 1)
          @Parameter(description = "The specific revision of the flow needed", example = "1")
          Long revision,
      @PathParam(ControllerConstants.PARAMETER_PSP)
          @Parameter(
              description = "The PSP identifier, used as a search filter",
              example = "88888888888")
          String pspId,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_INDEX)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_INDEX_DEFAULT)
          @Min(value = 1)
          @Parameter(description = "The index of the page to be shown in the result", example = "1")
          long pageNumber,
      @QueryParam(ControllerConstants.PARAMETER_PAGE_SIZE)
          @DefaultValue(ControllerConstants.PARAMETER_PAGE_SIZE_DEFAULT)
          @Min(value = 1)
          @Parameter(
              description = "The number of the elements of the page to be shown in the result",
              example = "50")
          long pageSize);
}
