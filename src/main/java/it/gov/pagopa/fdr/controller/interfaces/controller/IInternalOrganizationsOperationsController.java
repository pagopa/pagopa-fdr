package it.gov.pagopa.fdr.controller.interfaces.controller;

import it.gov.pagopa.fdr.controller.model.error.ErrorResponse;
import it.gov.pagopa.fdr.controller.model.flow.response.PaginatedFlowsResponse;
import it.gov.pagopa.fdr.util.constant.ControllerConstants;
import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.openapi.APIAppErrorMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISecurityMode;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.APISynchronism;
import it.gov.pagopa.fdr.util.openapi.APITableMetadata.ReadWrite;
import it.gov.pagopa.fdr.util.validator.PastDateLimit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Path(ControllerConstants.URL_CONTROLLER_INTERNAL_ORGS)
@Consumes("application/json")
@Produces("application/json")
@Tag(
    name = "Internal Operations",
    description = "APIs for internal operations, used for hidden processes in FdR ecosystem")
public interface IInternalOrganizationsOperationsController {

    @GET
    @Operation(
            operationId = "IInternalOperationsController_getAllPublishedFlowsForInternalUse",
            summary = "Get all published flow related to creditor institution (for internal processes)",
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
    PaginatedFlowsResponse getAllPublishedFlowsForInternalUse(
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
                                    + "dynamic default equal to the start of the previous 30th days at 00:00 UTC",
                    example = "2025-01-01T12:00:00Z")
            @PastDateLimit(value = 30, unit = ChronoUnit.DAYS)
            @QueryParam(ControllerConstants.PARAMETER_PUBLISHED_GREATER_THAN)
            Optional<Instant> publishedGt,
            @Parameter(
                    description =
                            "A date to be used as a lower limit search on flow date. In format"
                                    + " ISO-8601 (yyyy-MM-dd'T'HH:mm:ss). If omitted, the server uses"
                                    + " a dynamic default equal to the start of the previous 30th days at 00:00 UTC",
                    example = "2025-01-01T12:00:00Z")
            @PastDateLimit(value = 30, unit = ChronoUnit.DAYS)
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
            @Parameter(
                    description = "The number of the elements of the page to be shown in the result",
                    example = "50")
            long pageSize);
}
