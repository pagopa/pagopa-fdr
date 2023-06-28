package it.gov.pagopa.fdr.rest.psps.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.rest.psps.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.validation.CommonValidationService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
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
      String action,
      String psp,
      String fdr,
      CreateFlowRequest createFlowRequest,
      ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp sender
    String pspId = createFlowRequest.getSender().getPspId();
    checkPspSender(log, psp, pspId, createFlowRequest.getReportingFlowName());

    // check psp
    PaymentServiceProvider paymentServiceProvider =
        checkPaymentServiceProvider(log, psp, configData);

    // check broker
    String brokerId = createFlowRequest.getSender().getBrokerId();
    BrokerPsp brokerPsp = checkBrokerPsp(log, brokerId, configData);

    // check channel
    String channelId = createFlowRequest.getSender().getChannelId();
    Channel channel = checkChannel(log, channelId, configData);

    // check channel/broker
    checkChannelBrokerPsp(log, channel, channelId, brokerPsp, brokerId);

    // check channel/psp
    checkChannelPsp(log, channel, channelId, paymentServiceProvider, pspId, configData);

    // check ec
    String ecId = createFlowRequest.getReceiver().getEcId();
    checkCreditorInstitution(log, ecId, configData);

    // check reportingFlowName format
    String reportingFlowName = createFlowRequest.getReportingFlowName();
    checkFlowName(log, fdr, reportingFlowName);
    checkReportingFlowFormat(log, reportingFlowName, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateAddPayment(String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    // check reportingFlowName format
    checkReportingFlowFormat(log, fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validateDeletePayment(
      String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    /// check reportingFlowName format
    checkReportingFlowFormat(log, fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validatePublish(String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    /// check reportingFlowName format
    checkReportingFlowFormat(log, fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validateDelete(String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    /// check reportingFlowName format
    checkReportingFlowFormat(log, fdr, psp);
  }
}
