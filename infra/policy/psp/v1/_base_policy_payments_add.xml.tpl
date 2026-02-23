<policies>
    <inbound>
        <base />
        <choose>
            <when condition="@(!((string)context.Request.Headers.GetValueOrDefault("X-Orginal-Host-For","")).Equals("api.prf.platform.pagopa.it") && !((string)context.Request.OriginalUrl.ToUri().Host).Equals("api.prf.platform.pagopa.it"))">
                <rate-limit-by-key calls="600" renewal-period="60" counter-key="@(context.Subscription.Key)" increment-condition="@(context.Response.StatusCode != 429)" remaining-calls-header-name="x-remaining-calls-per-min" />
                <rate-limit-by-key calls="50" renewal-period="1" counter-key="@(context.Subscription.Key)" increment-condition="@(context.Response.StatusCode != 429)" remaining-calls-header-name="x-remaining-calls-per-sec" />
            </when>
        </choose>
    </inbound>
    <outbound>
        <base />
    </outbound>
</policies>