<policies>
    <inbound>
        <base />
        <set-backend-service base-url="https://${hostname}/pagopa-fdr-service-core" />
        <!-- Calling Authorizer's fragment -->
        <set-variable name="application_domain" value="fdr" />
        <choose>
            <!-- Making sure that will exclude all APIs that does not include CI fiscal code -->
            <when condition="@(context.Request.MatchedParameters.ContainsKey("organizationId"))">
                <set-variable name="authorization_entity" value="@(context.Request.MatchedParameters["organizationId"])" />
                <include-fragment fragment-id="authorizer" />
            </when>
        </choose>
    </inbound>
    <outbound>
        <base />
    </outbound>
    <backend>
        <base />
    </backend>
    <on-error>
        <base />
    </on-error>
</policies>
