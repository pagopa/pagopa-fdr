package it.gov.pagopa.fdr.util;

import java.text.MessageFormat;

public class AppUtil {
    public static String format(String message, Object... args){
        if( message == null ) {
            return null;
        } else {
            return MessageFormat.format(message, args);
        }
    }

}
