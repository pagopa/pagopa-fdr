package it.gov.pagopa.fdr.service.middleware.validator.clause;

import it.gov.pagopa.fdr.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.fdr.util.validator.ValidationArgs;
import it.gov.pagopa.fdr.util.validator.ValidationResult;
import it.gov.pagopa.fdr.util.validator.ValidationStep;
import java.util.Map;
import org.openapi.quarkus.api_config_cache_json.model.BrokerPsp;
import org.openapi.quarkus.api_config_cache_json.model.Channel;
import org.openapi.quarkus.api_config_cache_json.model.ConfigDataV1;
import org.openapi.quarkus.api_config_cache_json.model.PaymentServiceProvider;
import org.openapi.quarkus.api_config_cache_json.model.PspChannelPaymentType;

public class ChannelValidator extends ValidationStep {

  @Override
  public ValidationResult validate(ValidationArgs args) {

    ConfigDataV1 configData = args.getArgument("configDataV1", ConfigDataV1.class);
    String pspId = args.getArgument("pspId", String.class);
    String channelId = args.getArgument("channelId", String.class);
    String brokerPspId = args.getArgument("brokerPspId", String.class);

    // executing a lookup in cached configuration, check if channel exists
    Channel channel = configData.getChannels().get(channelId);
    if (channel == null) {
      return ValidationResult.asInvalid(AppErrorCodeMessageEnum.CHANNEL_UNKNOWN, channelId);
    }

    // executing a lookup in cached configuration, check if channel is enabled
    if (channel.getEnabled() == null || !channel.getEnabled()) {
      return ValidationResult.asInvalid(AppErrorCodeMessageEnum.CHANNEL_NOT_ENABLED, channelId);
    }

    // executing a lookup in cached configuration, check if channel is correctly linked to broker
    // PSP
    BrokerPsp brokerPsp = configData.getPspBrokers().get(brokerPspId);
    String brokerPspCode = brokerPsp.getBrokerPspCode();
    if (channel.getBrokerPspCode() == null || !channel.getBrokerPspCode().equals(brokerPspCode)) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.CHANNEL_BROKER_WRONG_CONFIG, channelId, brokerPspId);
    }

    // executing a lookup in cached configuration, check if there is a valid configuration on PSP
    // for channel about payment type
    PaymentServiceProvider psp = configData.getPsps().get(pspId);
    Map<String, PspChannelPaymentType> paymentTypes = configData.getPspChannelPaymentTypes();
    boolean existsChannelConfiguration =
        paymentTypes.entrySet().stream()
            .anyMatch(entry -> checkConfigurationOnPaymentType(entry.getValue(), channel, psp));
    if (!existsChannelConfiguration) {
      return ValidationResult.asInvalid(
          AppErrorCodeMessageEnum.CHANNEL_PSP_WRONG_CONFIG, channelId, pspId);
    }

    return this.checkNext(args);
  }

  private static boolean checkConfigurationOnPaymentType(
      PspChannelPaymentType paymentType, Channel channel, PaymentServiceProvider psp) {

    String channelCode = channel.getChannelCode();
    String pspCode = psp.getPspCode();
    boolean i1 = paymentType.getPspCode() != null && paymentType.getPspCode().equals(pspCode);
    boolean i2 =
        paymentType.getChannelCode() != null && paymentType.getChannelCode().equals(channelCode);
    return i1 && i2;
  }
}
