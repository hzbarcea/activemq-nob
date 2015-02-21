package org.apache.activemq.nob.filestore.uuiddir;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.filestore.BrokerMetadataLoader;
import org.apache.activemq.nob.filestore.BrokerMetadataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by art on 2/19/15.
 */
public class UUIDBrokerMetadataWriter implements BrokerMetadataWriter {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(UUIDBrokerMetadataWriter.class);

    private Logger LOG = DEFAULT_LOGGER;

    public Logger getLOG() {
        return LOG;
    }

    public void setLOG(Logger LOG) {
        this.LOG = LOG;
    }

    @Override
    public void writeBrokerMetadata(File metadataPath, Broker broker) {
        Properties p = new Properties();
        p.setProperty("id", broker.getName());
        p.setProperty("name", broker.getName());
        p.setProperty("status", broker.getStatus());

        try {
            OutputStream out = new FileOutputStream(metadataPath);
            p.store(out, "# Broker definition for " + broker.getName());
        } catch (IOException e) {
            LOG.error("Failed to store broker metadata for '{}': {}", broker.getName(), e.getMessage());
        }
    }
}
