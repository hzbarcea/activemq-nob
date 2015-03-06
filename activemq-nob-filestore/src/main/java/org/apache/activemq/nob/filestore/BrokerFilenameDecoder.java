package org.apache.activemq.nob.filestore;

import java.io.File;
import java.util.UUID;

/**
 * Created by art on 2/19/15.
 */
public interface BrokerFilenameDecoder {

    /**
     * Extract the ID of the broker from the file at the given path. The file
     * may be a directory or a regular file.
     *
     * @param brokerPath path to the candidate broker.
     * @return ID of the broker at that path, if the path is a valid broker
     * path.
     */
    String extractIdFromFilename(File brokerPath);

    /**
     * Locate the properties file for the broker at the given path.
     *
     * @param brokerPath path to the broker.
     * @return path to the properties configuration file for the broker.
     */
    File getBrokerPropertiesFile(File brokerPath);

    /**
     * Locate the Xbean configuration file for the broker at the given path.
     *
     * @param brokerPath path to the broker.
     * @return path to the Xbean configuration file for the broker.
     */
    File getBrokerXbeanFile(File brokerPath);
}
