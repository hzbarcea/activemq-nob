package org.apache.activemq.nob.filestore;

import java.io.File;

/**
 * Created by art on 2/19/15.
 */
public interface BrokerFilenameEncoder {
    /**
     * Locate the metadata file for the broker with the specified ID.
     *
     * @param brokerId of the broker for which to locate the metadata file.
     * @return path to the metadata file for the broker with this given ID, even if the broker does not exist.
     */
    File locateBrokerMetadataFilePath(String brokerId);

    /**
     * Locate the xbean configuration file for the broker with the specified ID.
     *
     * @param brokerId of the broker for which to locate the xbean file.
     * @return path to the xbean file for the broker with this given ID, even if the broker does not exist.
     */
    File locateBrokerXbeanFilePath(String brokerId);
}
