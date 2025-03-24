<policies>
    <inbound>
        <base />
        <rate-limit-by-key calls="300"
              renewal-period="60"
              increment-condition="@(context.Response.StatusCode != 429)"
              counter-key="@(context.Subscription.Key)"/>
        <rate-limit-by-key calls="10"
              renewal-period="1"
              increment-condition="@(context.Response.StatusCode != 429)"
              counter-key="@(context.Subscription.Key)"/>
    </inbound>
    <outbound>
        <base />
    </outbound>
</policies>