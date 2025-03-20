<policies>
    <inbound>
        <base />
        <rate-limit-by-key calls="10"
              renewal-period="1"
              increment-condition="@(context.Response.StatusCode == 200)"
              counter-key="@(context.Subscription.Key)"
              remaining-calls-variable-name="remainingCallsPerSubscriptionKeySecond"/>
    </inbound>
    <outbound>
        <base />
    </outbound>
</policies>