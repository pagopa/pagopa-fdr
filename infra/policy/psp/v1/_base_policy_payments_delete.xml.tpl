<policies>
    <inbound>
        <base />
        <choose>
            <when condition="@(!((string)context.Request.Headers.GetValueOrDefault("X-Orginal-Host-For","")).Equals("api.prf.platform.pagopa.it") && !((string)context.Request.OriginalUrl.ToUri().Host).Equals("api.prf.platform.pagopa.it"))">
                <rate-limit-by-key calls="10"
                   renewal-period="1"
                   increment-condition="@(context.Response.StatusCode != 429)"
                   counter-key="@(context.Subscription.Key)"/>
            </when>
        </choose>
    </inbound>
    <outbound>
        <base />
    </outbound>
</policies>