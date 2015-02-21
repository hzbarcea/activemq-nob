package org.apache.activemq.nob.filestore;

import org.apache.activemq.nob.api.Broker;

import java.io.File;

/**
 * Created by art on 2/19/15.
 */
public interface BrokerMetadataLoader {
    Broker loadMetadata(File metadataPath, File xbeanPath);
}
