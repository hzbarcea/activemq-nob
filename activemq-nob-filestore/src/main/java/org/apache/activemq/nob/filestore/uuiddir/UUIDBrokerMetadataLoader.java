package org.apache.activemq.nob.filestore.uuiddir;

import org.apache.activemq.nob.filestore.BrokerMetadataLoader;
import org.apache.activemq.nob.api.Broker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by art on 2/19/15.
 */
public class UUIDBrokerMetadataLoader implements BrokerMetadataLoader {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(UUIDBrokerMetadataLoader.class);

    private Logger LOG = DEFAULT_LOGGER;

    public Logger getLOG() {
        return LOG;
    }

    public void setLOG(Logger LOG) {
        this.LOG = LOG;
    }

    @Override
    public Broker loadMetadata(File metadataPath, File xbeanPath) {
        Properties p = new Properties();

        try {
            InputStream in = new FileInputStream(metadataPath);
            p.load(in);
        } catch (IOException e) {
            return null;
        }

        Broker broker = new Broker();
        broker.setId(p.getProperty("id"));
        broker.setName(p.getProperty("name"));
        broker.setStatus(p.getProperty("status"));

        // Right now we don't store the lastModified timestamp, we retrive from fs
        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(xbeanPath.lastModified());
        broker.setLastModifiedXbean(lastModified);

        if (!metadataPath.getName().equals(broker.getName())) {
            LOG.warn("Broker definition error in {}. Mismatched id={}. ", metadataPath.getName(), broker.getName());
            return null;
        }

        return broker;
    }
}
