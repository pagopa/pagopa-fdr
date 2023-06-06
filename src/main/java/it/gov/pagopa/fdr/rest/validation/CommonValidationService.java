package it.gov.pagopa.fdr.rest.validation;

import it.gov.pagopa.fdr.exception.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.exception.AppException;
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
public class CommonValidationService {

  @Inject Logger log;

  public void checkPspSender(String psp, String pspId, String reportingFlowName) {
    if (!psp.equals(pspId)) {
      throw new AppException(
          AppErrorCodeMessageEnum.REPORTING_FLOW_PSP_ID_NOT_MATCH, reportingFlowName, pspId, psp);
    }
  }

  public PaymentServiceProvider checkPaymentServiceProvider(String psp, ConfigDataV1 configData) {
    PaymentServiceProvider paymentServiceProvider =
        Optional.ofNullable(configData.getPsps().get(psp))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.PSP_UNKNOWN, psp));

    if (paymentServiceProvider.getEnabled() == null || !paymentServiceProvider.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.PSP_NOT_ENABLED, psp);
    }
    return paymentServiceProvider;
  }

  public BrokerPsp checkBrokerPsp(String brokerId, ConfigDataV1 configData) {
    BrokerPsp brokerPsp =
        Optional.ofNullable(configData.getPspBrokers().get(brokerId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.BROKER_UNKNOWN, brokerId));

    if (brokerPsp.getEnabled() == null || !brokerPsp.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.BROKER_NOT_ENABLED, brokerId);
    }
    return brokerPsp;
  }

  public Channel checkChannel(String channelId, ConfigDataV1 configData) {
    Channel channel =
        Optional.ofNullable(configData.getChannels().get(channelId))
            .orElseThrow(
                () -> new AppException(AppErrorCodeMessageEnum.CHANNEL_UNKNOWN, channelId));

    if (channel.getEnabled() == null || !channel.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.CHANNEL_NOT_ENABLED, channelId);
    }
    return channel;
  }

  public void checkChannelBrokerPsp(
      Channel channel, String channelId, BrokerPsp brokerPsp, String brokerId) {
    if (channel.getBrokerPspCode() == null
        || !channel.getBrokerPspCode().equals(brokerPsp.getBrokerPspCode())) {
      throw new AppException(
          AppErrorCodeMessageEnum.CHANNEL_BROKER_WRONG_CONFIG, channelId, brokerId);
    }
  }

  public void checkChannelPsp(
      Channel channel,
      String channelId,
      PaymentServiceProvider paymentServiceProvider,
      String pspId,
      ConfigDataV1 configData) {
    boolean exist =
        configData.getPspChannelPaymentTypes().entrySet().stream()
            .anyMatch(
                a -> {
                  PspChannelPaymentType value = a.getValue();
                  return value.getPspCode().equals(paymentServiceProvider.getPspCode())
                      && value.getChannelCode().equals(channel.getChannelCode());
                });
    if (!exist) {
      throw new AppException(AppErrorCodeMessageEnum.CHANNEL_PSP_WRONG_CONFIG, channelId, pspId);
    }
  }

  public void checkCreditorInstitution(String ecId, ConfigDataV1 configData) {
    CreditorInstitution ec =
        Optional.ofNullable(configData.getCreditorInstitutions().get(ecId))
            .orElseThrow(() -> new AppException(AppErrorCodeMessageEnum.EC_UNKNOWN, ecId));

    if (ec.getEnabled() == null || !ec.getEnabled()) {
      throw new AppException(AppErrorCodeMessageEnum.EC_NOT_ENABLED, ecId);
    }
  }

  public void checkReportingFlowFormat(String reportingFlowName, String pspId) {
    String date = reportingFlowName.substring(0, 10);
    try {
      // default, ISO_LOCAL_DATE ("2016-08-16")
      LocalDate.parse(date);
    } catch (RuntimeException e) {
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
}
