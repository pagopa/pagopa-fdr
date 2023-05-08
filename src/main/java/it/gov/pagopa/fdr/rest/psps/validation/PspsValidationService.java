package it.gov.pagopa.fdr.rest.psps.validation;

import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
import it.gov.pagopa.fdr.rest.psps.request.AddPaymentRequest;
import it.gov.pagopa.fdr.rest.psps.request.CreateFlowRequest;
import it.gov.pagopa.fdr.rest.psps.request.DeletePaymentRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.Channel;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.CreditorInstitution;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;
import org.openapi.quarkus.api_config_cache_json.model.PspChannelPaymentType;

@ApplicationScoped
public class PspsValidationService {

  @Inject Logger log;

  @WithSpan(kind = SERVER)
  public void validateCreateFlow(
      String psp, CreateFlowRequest createFlowRequest, ConfigDataV1 configData) {
    log.debug("Validate create");

    String reportingFlowName = createFlowRequest.getReportingFlowName();

    // check psp
    String pspId = createFlowRequest.getSender().getPspId();
    if (!psp.equals(pspId)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_ID_NOT_MATCH, reportingFlowName, pspId, psp);
    }

    PaymentServiceProvider paymentServiceProvider =
        Optional.ofNullable(configData.getPsps().get(pspId))
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_UNKNOWN,
                        pspId,
                        reportingFlowName));

    if (!paymentServiceProvider.getEnabled()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_NOT_ENABLED, pspId, reportingFlowName);
    }

    // check broker
    String brokerId = createFlowRequest.getSender().getBrokerId();
    BrokerPsp brokerPsp =
        Optional.ofNullable(configData.getPspBrokers().get(brokerId))
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_BROKER_UNKNOWN,
                        brokerId,
                        reportingFlowName));

    if (!brokerPsp.getEnabled()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_BROKER_NOT_ENABLED, brokerId, reportingFlowName);
    }

    // check channel
    String channelId = createFlowRequest.getSender().getChannelId();
    Channel channel =
        Optional.ofNullable(configData.getChannels().get(channelId))
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_CHANNEL_UNKNOWN,
                        channelId,
                        reportingFlowName));

    if (!channel.getEnabled()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_CHANNEL_NOT_ENABLED, channelId, reportingFlowName);
    }

    // check channel/broker
    if (!channel.getBrokerPspCode().equals(brokerPsp.getBrokerPspCode())) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_CHANNEL_BROKER_WRONG_CONFIG,
          channelId,
          brokerId,
          reportingFlowName);
    }

    // check channel/psp
    boolean notExist =
        configData.getPspChannelPaymentTypes().entrySet().stream()
                .filter(
                    a -> {
                      PspChannelPaymentType value = a.getValue();
                      return value.getPspCode().equals(paymentServiceProvider.getPspCode())
                          && value.getChannelCode().equals(channel.getChannelCode());
                    })
                .count()
            == 0;
    if (notExist) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_CHANNEL_PSP_WRONG_CONFIG,
          channelId,
          pspId,
          reportingFlowName);
    }

    // check ec
    String ecId = createFlowRequest.getReceiver().getEcId();
    CreditorInstitution ec =
        Optional.ofNullable(configData.getCreditorInstitutions().get(ecId))
            .orElseThrow(
                () ->
                    new AppException(
                        AppErrorCodeMessageEnum.REPORTING_FLOW_EC_UNKNOWN,
                        ecId,
                        reportingFlowName));

    if (!ec.getEnabled()) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_EC_NOT_ENABLED, ecId, reportingFlowName);
    }

    // check reportingFlowName format
    String date = reportingFlowName.substring(0, 10);
    try {
      // default, ISO_LOCAL_DATE ("2016-08-16")
      LocalDate.parse(date);
    } catch (Throwable e) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_DATE_WRONG_FORMAT, reportingFlowName);
    }
    // TODO non dovremmo verificare che la data sia uguale a regulationDate o reportingFlowDate

    String name = reportingFlowName.substring(10);
    boolean nameWrongFromat = !name.startsWith(String.format("%s-", pspId));
    if (nameWrongFromat) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_NAME_PSP_WRONG_FORMAT, reportingFlowName);
    }
  }

  @WithSpan(kind = SERVER)
  public void validateAddPayment(String psp, String fdr, AddPaymentRequest addPaymentRequest) {
    log.debug("Validate add payment");
  }

  @WithSpan(kind = SERVER)
  public void validateDeletePayment(
      String psp, String fdr, DeletePaymentRequest deletePaymentRequest) {
    log.debug("Validate delete payment");
  }

  @WithSpan(kind = SERVER)
  public void validatePublish(String psp, String fdr) {
    log.debug("Validate publish");
  }

  @WithSpan(kind = SERVER)
  public void validateDelete(String psp, String fdr) {
    log.debug("Validate delete");
  }
}
