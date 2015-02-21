package org.apache.activemq.nob.persistence.api.exception;

/**
 * Base exception for all exceptions thrown by broker configuration operations.
 *
 * Created by art on 1/11/15.
 */
public abstract class BrokerConfigException extends Exception {
    public BrokerConfigException() {
    }

    public BrokerConfigException(String message) {
        super(message);
    }

    public BrokerConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokerConfigException(Throwable cause) {
        super(cause);
    }
}
