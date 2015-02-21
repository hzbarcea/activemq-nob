package org.apache.activemq.nob.persistence.api.exception;

/**
 * Exception indicating the configuration for a broker was not found.
 *
 * Created by art on 1/11/15.
 */
public class BrokerConfigNotFoundException extends BrokerConfigException {
    public BrokerConfigNotFoundException() {
    }

    public BrokerConfigNotFoundException(String message) {
        super(message);
    }

    public BrokerConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokerConfigNotFoundException(Throwable cause) {
        super(cause);
    }
}
