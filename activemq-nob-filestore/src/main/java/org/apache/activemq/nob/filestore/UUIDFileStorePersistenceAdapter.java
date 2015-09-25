/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.nob.filestore;

import java.io.File;
import org.apache.activemq.nob.filestore.exception.FileStoreConfigurationException;
import org.apache.activemq.nob.filestore.uuiddir.UUIDBrokerMetadataLoader;
import org.apache.activemq.nob.filestore.uuiddir.UUIDBrokerMetadataWriter;
import org.apache.activemq.nob.filestore.uuiddir.UUIDDirectoryStoreFilenameDecoder;
import org.apache.activemq.nob.filestore.uuiddir.UUIDDirectoryStoreFilenameEncoder;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File persistence adapter using UUIDs for broker names.
 *
 * @author Ciprian Ciubotariu <cheepeero@gmx.net>
 */
public class UUIDFileStorePersistenceAdapter extends FileStoreServerPersistenceAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(UUIDFileStorePersistenceAdapter.class);

    /**
     * Initialize the persistence.
     *
     * Used by OSGi Config Admin after (re)configuring the location.
     *
     * @throws
     * org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException
     */
    @Override
    public void init() throws BrokerConfigException {
        File location = getLocation();

        if (!location.exists()) {
            location.mkdirs();
        };

        if (!location.isDirectory()) {
            throw new FileStoreConfigurationException("cannot use " + location + " for storage: not a folder");
        }

        LOG.info("Using NOB data at {}", location.getAbsolutePath());

        this.setBrokerFilenameDecoder(new UUIDDirectoryStoreFilenameDecoder());
        this.setBrokerFilenameEncoder(new UUIDDirectoryStoreFilenameEncoder(location));
        this.setBrokerMetadataLoader(new UUIDBrokerMetadataLoader());
        this.setBrokerMetadataWriter(new UUIDBrokerMetadataWriter());

        super.init();
    }
}
