package org.apache.activemq.nob.persistence.api.exception;

/**
 * Base exception for all exceptions thrown by broker configuration persistence operations.
 * Created by art on 2/19/15.
 */
public abstract class BrokerConfigPersistenceException extends BrokerConfigException {
    public BrokerConfigPersistenceException() {
    }

    public BrokerConfigPersistenceException(String message) {
        super(message);
    }

    public BrokerConfigPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokerConfigPersistenceException(Throwable cause) {
        super(cause);
    }
}
