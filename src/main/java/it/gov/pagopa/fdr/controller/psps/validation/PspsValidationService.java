package it.gov.pagopa.fdr.controller.psps.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.controller.psps.request.CreateRequest;
import it.gov.pagopa.fdr.controller.validation.CommonValidationService;
import it.gov.pagopa.fdr.util.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.Channel;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;

@ApplicationScoped
public class PspsValidationService extends CommonValidationService {

  private final Logger log;

  public PspsValidationService(Logger log) {
    this.log = log;
  }

  @WithSpan(kind = SERVER)
  public void validateCreateFlow(
      String action, String psp, String fdr, CreateRequest createRequest, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp sender
    String pspId = createRequest.getSender().getPspId();
    checkPspSender(log, psp, pspId, createRequest.getFdr());

    // check psp
    PaymentServiceProvider paymentServiceProvider =
        checkPaymentServiceProvider(log, psp, configData);

    // check broker
    String brokerId = createRequest.getSender().getPspBrokerId();
    BrokerPsp brokerPsp = checkBrokerPsp(log, brokerId, configData);

    // check channel
    String channelId = createRequest.getSender().getChannelId();
    Channel channel = checkChannel(log, channelId, configData);

    // check channel/broker
    checkChannelBrokerPsp(log, channel, channelId, brokerPsp, brokerId);

    // check channel/psp
    checkChannelPsp(log, channel, channelId, paymentServiceProvider, pspId, configData);

    // check ec
    String ecId = createRequest.getReceiver().getOrganizationId();
    checkCreditorInstitution(log, ecId, configData);

    // check fdr format
    String createRequestFdr = createRequest.getFdr();
    checkFlowName(log, fdr, createRequestFdr);
    checkReportingFlowFormat(log, createRequestFdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateAddPayment(String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    // check fdr format
    checkReportingFlowFormat(log, fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validateDeletePayment(
      String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    /// check fdr format
    checkReportingFlowFormat(log, fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validatePublish(String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    /// check fdr format
    checkReportingFlowFormat(log, fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validateDelete(String action, String psp, String fdr, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, psp, configData);

    /// check fdr format
    checkReportingFlowFormat(log, fdr, psp);
  }

  @WithSpan(kind = SERVER)
  public void validateGetAllPublished(
      String action, String pspId, String ecId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check ec
    if (null != pspId && !pspId.isBlank()) {
      checkCreditorInstitution(log, ecId, configData);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateGetPublished(
      String action, String fdr, String pspId, String ecId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check ec
    checkCreditorInstitution(log, ecId, configData);

    // check fdr format
    checkReportingFlowFormat(log, fdr, pspId);
  }

  @WithSpan(kind = SERVER)
  public void validateGetPaymentPublished(
      String action, String fdr, String pspId, String ecId, ConfigDataV1 configData) {
    log.info(AppMessageUtil.logValidate(action));

    // check psp
    checkPaymentServiceProvider(log, pspId, configData);

    // check ec
    checkCreditorInstitution(log, ecId, configData);

    // check fdr format
    checkReportingFlowFormat(log, fdr, pspId);
  }
}
