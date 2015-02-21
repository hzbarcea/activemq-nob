package org.apache.activemq.nob.filestore;

import org.apache.activemq.nob.filestore.uuiddir.UUIDBrokerMetadataLoader;
import org.apache.activemq.nob.filestore.uuiddir.UUIDBrokerMetadataWriter;
import org.apache.activemq.nob.filestore.uuiddir.UUIDDirectoryStoreFilenameDecoder;
import org.apache.activemq.nob.filestore.uuiddir.UUIDDirectoryStoreFilenameEncoder;

import java.io.File;

/**
 * Configure a default FileStorePersistenceAdapter which uses the files named as UUIDs for the broker metadata.
 *
 * Created by art on 2/19/15.
 */
public class DefaultFileStorePersistenceAdapter extends FileStoreServerPersistenceAdapter {
    public DefaultFileStorePersistenceAdapter(File location) {
        this.setLocation(location);

        this.setBrokerFilenameDecoder(new UUIDDirectoryStoreFilenameDecoder());
        this.setBrokerFilenameEncoder(new UUIDDirectoryStoreFilenameEncoder(location));
        this.setBrokerMetadataLoader(new UUIDBrokerMetadataLoader());
        this.setBrokerMetadataWriter(new UUIDBrokerMetadataWriter());
    }
}
