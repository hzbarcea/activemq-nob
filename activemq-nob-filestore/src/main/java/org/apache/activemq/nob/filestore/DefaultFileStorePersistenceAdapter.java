package org.apache.activemq.nob.filestore;

import java.io.File;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;

/**
 * Configure a default FileStorePersistenceAdapter which uses the files named as UUIDs for the broker metadata.
 *
 * Created by art on 2/19/15.
 */
public class DefaultFileStorePersistenceAdapter extends UUIDFileStorePersistenceAdapter {
    public DefaultFileStorePersistenceAdapter(File location) throws BrokerConfigException {
        this.setLocation(location);

        super.init();
    }
}
