package org.apache.activemq.nob.filestore.exception;

import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;

/**
 * Created by art on 2/19/15.
 */
public class FileStoreWriteBrokerException extends BrokerConfigPersistenceException {
    public FileStoreWriteBrokerException() {
    }

    public FileStoreWriteBrokerException(String message) {
        super(message);
    }

    public FileStoreWriteBrokerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStoreWriteBrokerException(Throwable cause) {
        super(cause);
    }
}
