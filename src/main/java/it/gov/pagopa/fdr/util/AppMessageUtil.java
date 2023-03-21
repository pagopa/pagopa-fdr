package it.gov.pagopa.fdr.util;

import io.quarkus.logging.Log;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppMessageUtil {
  private static final Locale defaultLocale = new Locale("en");

  private static ResourceBundle getBundle(Locale locale) {
    if (locale == null) {
      locale = Optional.of(Locale.getDefault()).orElse(defaultLocale);
    }
    ResourceBundle bundle = null;
    try {
      bundle = ResourceBundle.getBundle("messages", locale);
    } catch (Exception e) {
      Log.debug(
          String.format(
              "Not found bundle message_%s.properties. Load default messages_%s.properties",
              locale, defaultLocale));
      bundle = ResourceBundle.getBundle("messages", defaultLocale);
    }
    return bundle;
  }

  public static String getMessage(String messageKey, Locale locale) {
    String message = "messageKey not found !!!";
    try {
      message = getBundle(locale).getString(messageKey);
    } catch (Exception e) {
      Log.error(String.format("Error while getting message for messageKey %s", messageKey), e);
    }
    return message;
  }

  public static String getMessage(String messageKey) {
    return getMessage(messageKey, (Locale) null);
  }

  public static String getMessage(String messageKey, Locale locale, Object... messageArguments) {
    return MessageFormat.format(getMessage(messageKey, locale), messageArguments);
  }

  public static String getMessage(String messageKey, Object... messageArguments) {
    return getMessage(messageKey, null, messageArguments);
  }
}
