package it.gov.pagopa.fdr.rest.psps.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.psps.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.validation.CommonValidationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.Channel;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;

@ApplicationScoped
public class PspsValidationService extends CommonValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateCreateFlow(
      String psp, CreateFlowRequest createFlowRequest, ConfigDataV1 configData) {
    log.debug("Validate create");

    // check psp sender
    String pspId = createFlowRequest.getSender().getPspId();
    checkPspSender(psp, pspId, createFlowRequest.getReportingFlowName());

    // check psp
    PaymentServiceProvider paymentServiceProvider = checkPaymentServiceProvider(psp, configData);

    // check broker
    String brokerId = createFlowRequest.getSender().getBrokerId();
    BrokerPsp brokerPsp = checkBrokerPsp(brokerId, configData);

    // check channel
    String channelId = createFlowRequest.getSender().getChannelId();
    Channel channel = checkChannel(channelId, configData);

    // check channel/broker
    checkChannelBrokerPsp(channel, channelId, brokerPsp, brokerId);

    // check channel/psp
    checkChannelPsp(channel, channelId, paymentServiceProvider, pspId, configData);

    // check ec
    String ecId = createFlowRequest.getReceiver().getEcId();
    checkCreditorInstitution(ecId, configData);

    // check reportingFlowName format
    String reportingFlowName = createFlowRequest.getReportingFlowName();
    checkReportingFlowFormat(reportingFlowName, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateAddPayment(String psp, String fdr, ConfigDataV1 configData) {
    log.debug("Validate add payment");

    // check psp
    checkPaymentServiceProvider(psp, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validateDeletePayment(String psp, String fdr, ConfigDataV1 configData) {
    log.debug("Validate delete payment");

    // check psp
    checkPaymentServiceProvider(psp, configData);

    /// check reportingFlowName format
    checkReportingFlowFormat(fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validatePublish(String psp, String fdr, ConfigDataV1 configData) {
    log.debug("Validate publish");

    // check psp
    checkPaymentServiceProvider(psp, configData);

    /// check reportingFlowName format
    checkReportingFlowFormat(fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validateDelete(String psp, String fdr, ConfigDataV1 configData) {
    log.debug("Validate delete");

    // check psp
    checkPaymentServiceProvider(psp, configData);

    /// check reportingFlowName format
    checkReportingFlowFormat(fdr, psp);
  }
}
