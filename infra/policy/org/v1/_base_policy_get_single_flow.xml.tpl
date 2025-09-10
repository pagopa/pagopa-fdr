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
        <set-body>@{
            // Parse the response body as a JObject to easily manipulate the JSON.
            var body = context.Response.Body.As<JObject>();

            // Check if the 'regulationDate' field exists and is not null to avoid errors.
            if (body["regulationDate"] != null)
            {
                // Get the current value of the regulationDate.
                var regulationDate = body["regulationDate"].ToString();

                // Append the time string to the date.
                body["regulationDate"] = regulationDate + "T00:00:00.000000Z";
            }

            // Convert the modified JObject back to a string and set it as the new response body.
            return body.ToString();
        }</set-body>
    </outbound>
    <on-error>
        <base />
    </on-error>
</policies>
