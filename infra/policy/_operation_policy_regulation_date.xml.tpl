<policies>
    <inbound>
        <base />
    </inbound>
    <backend>
        <base />
    </backend>
    <outbound>
        <base />
        <!-- This policy intercepts the outbound response, parses the JSON body,
             and appends a static time value to the regulationDate field. -->
        <set-variable name="application_domain" value="fdr" />
        <set-variable name="orgs_mitigated" value="00493410583" />
        <set-variable name="current_org" value="@(context.Request.MatchedParameters["organizationId"])" />
        <!--
        <choose>
            <when condition="@(((string)context.Variables.GetValueOrDefault("orgs_mitigated", "")).Contains((string)context.Variables.GetValueOrDefault("current_org", "UNDEFINED")))">
        -->
                <set-body>@{
                    // Parse the response body as a JObject to easily manipulate the JSON.
                    var body = context.Response.Body.As<JObject>();

                    // Check if the 'regulationDate' field exists and is not null to avoid errors.
                    if (body["regulationDate"] != null)
                    {
                    // Get the current value of the regulationDate.
                    var regulationDate = body["regulationDate"].ToString();

                    // Append the time to the date in ISO 8601 format with CEST (UTC+2): YYYY-MM-DDThh:mm:ss.ssssss+02:00.
                    body["regulationDate"] = regulationDate + "T00:00:00.000000+02:00";
                    }

                    // Convert the modified JObject back to a string and set it as the new response body.
                    return body.ToString();
                    }</set-body>
        <!--
            </when>
        </choose>
        -->
    </outbound>
    <on-error>
        <base />
    </on-error>
</policies>