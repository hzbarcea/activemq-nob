package org.apache.activemq.nob.filestore.uuiddir;

import org.apache.activemq.nob.filestore.BrokerFilenameEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Decoder of filenames in a UUID-based filesystem store of broker configuration files.  This store only supports
 * broker IDs in the form of UUIDs.
 *
 * Created by art on 2/19/15.
 */
public class UUIDDirectoryStoreFilenameEncoder implements BrokerFilenameEncoder {
    public static final String  XBEAN_FILE_PATH_SUFFIX = "-xbean.xml";
    public static final String  PROPERTIES_FILE_PATH_SUFFIX = ".properties";

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(UUIDDirectoryStoreFilenameEncoder.class);

    private Logger LOG = DEFAULT_LOGGER;
    private File root;

    public UUIDDirectoryStoreFilenameEncoder(File root) {
        this.root = root;
    }

    @Override
    public File locateBrokerMetadataFilePath(String brokerId) {
        File result = new File(this.root, brokerId);

        return  result;
    }

    @Override
    public File locateBrokerPropertiesFilePath(String brokerId) {
        File result = new File(this.root, brokerId + PROPERTIES_FILE_PATH_SUFFIX);

        return  result;
    }
    
    @Override
    public File locateBrokerXbeanFilePath(String brokerId) {
        File result = new File(this.root, brokerId + XBEAN_FILE_PATH_SUFFIX);

        return  result;
    }
}
