package org.apache.activemq.nob.filestore.exception;

import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;

/**
 * Created by art on 2/19/15.
 */
public class FileStoreLoadBrokerException extends BrokerConfigPersistenceException {
    public FileStoreLoadBrokerException() {
    }

    public FileStoreLoadBrokerException(String message) {
        super(message);
    }

    public FileStoreLoadBrokerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStoreLoadBrokerException(Throwable cause) {
        super(cause);
    }
}
