package org.apache.activemq.nob.filestore;

import org.apache.activemq.nob.api.Broker;

import java.io.File;

/**
 * Created by art on 2/19/15.
 */
public interface BrokerMetadataWriter {
    /**
     * Writer the broker metadata at the given path.
     *
     * @param metadataPath path to the metadata file to write.
     * @param broker details for the broker.
     */
    void writeBrokerMetadata(File metadataPath, Broker broker);
}
