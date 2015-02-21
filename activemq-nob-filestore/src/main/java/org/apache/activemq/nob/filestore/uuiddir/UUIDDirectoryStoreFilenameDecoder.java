package org.apache.activemq.nob.filestore.uuiddir;

import org.apache.activemq.nob.filestore.BrokerFilenameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

/**
 * Decoder of filenames in a UUID-based filesystem store of broker configuration files.  This store only supports
 * broker IDs in the form of UUIDs.
 *
 * Created by art on 2/19/15.
 */
public class UUIDDirectoryStoreFilenameDecoder implements BrokerFilenameDecoder {
    public static final String  XBEAN_FILE_PATH_SUFFIX = "-xbean.xml";

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(UUIDDirectoryStoreFilenameDecoder.class);

    private Logger LOG = DEFAULT_LOGGER;


    /**
     * Decode the pathname as a UUID if it is a regular file (i.e. not a directory) and return the UUID.
     *
     * @param brokerPath path to the candidate broker.
     * @return
     */
    @Override
    public String extractIdFromFilename(File brokerPath) {
        String result = null;

        if ( ! brokerPath.isDirectory() ) {
            try {
                UUID uuid = UUID.fromString(brokerPath.getName());

                if (uuid != null) {
                    result = uuid.toString();
                }
            } catch ( IllegalArgumentException illegalArgExc ) {
                LOG.debug("invalid UUID {}", brokerPath.getName());
            }
        }

        return result;
    }

    /**
     * Locate the path to the xbean configuration file for the broker at the given path.  This method validates the
     * broker path as it must to determine the broker ID.
     *
     * @param brokerPath path to the broker.
     * @return path to the xbean configuration file, even if it does not exist.
     */
    @Override
    public File getBrokerXbeanFile(File brokerPath) {
        File result = null;

        String brokerId = this.extractIdFromFilename(brokerPath);

        if ( brokerId != null ) {
            result = new File(brokerPath.getPath() + XBEAN_FILE_PATH_SUFFIX);
        }

        return result;
    }
}
