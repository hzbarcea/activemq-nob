/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.api.Brokers;
import org.apache.activemq.nob.api.Supervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JAX-RS ControlCenter root resource
 */
public class SupervisorService implements Supervisor {
     private static final Logger LOG = LoggerFactory.getLogger(SupervisorService.class);
;

    /**
     * Folder where the broker config structure is.
     */
    private File location;
    private Brokers brokers;

    public SupervisorService() {
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public void init() throws RuntimeException {
        if (location == null) {
            location = getDataLocation();
            if (location == null) {
                throw new RuntimeException("Cannot access NOB data");
            }
        }
        LOG.info("Using NOB data at {}", location.getAbsolutePath());
        refreshData();
    }

    public Response createBroker() {
        Broker broker = new Broker();
        broker.setName(UUID.randomUUID().toString());
        storeBrokerMetadata(broker);
        brokers.getBrokers().add(broker);
        return Response.ok().type(MediaType.APPLICATION_XML).entity(broker.getName()).build();
    }

    public Brokers getBrokers(String filter) {
        return brokers;
    }

    @Override
    public Broker getBroker(String brokerid) {
        for (Broker broker : brokers.getBrokers()) {
            if (broker.getName().equals(brokerid)) {
                return broker;
            }
        }
        return null;
    }

    public void updateBroker(String brokerid, Broker brokertype) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void deleteBroker(String brokerid) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Response getBrokerXbeanConfig(String brokerid) {
        File xbeanFile = getBrokerXbeanFile(brokerid);
        return (xbeanFile != null) ?
            Response.ok().type(MediaType.APPLICATION_XML).entity(xbeanFile).build() :
            Response.status(Response.Status.NOT_FOUND).build();
            // TODO: add lastUpdated header !
    }

    public Response getBrokerStatus(String brokerid) {
        // TODO Auto-generated method stub
        return null;
    }


    public static File getDataLocation() {
        File dataLocation;
        String envData = System.getenv("NOB_DATA");
        if (envData == null) {
            System.getProperty("user.home");
            dataLocation = new File(new File(System.getProperty("user.home")), ".nob");
            if (!dataLocation.exists()) {
                dataLocation = new File(new File("."), ".nob");
            }
        } else {
            dataLocation = new File(envData);
        }
        if (!dataLocation.exists()) {
            dataLocation.mkdirs();
        };
        
        if (!dataLocation.isDirectory()) {
            LOG.error("Cannot create data directory {}", dataLocation.getAbsolutePath());
            return null;
        }
        return dataLocation;
    }


    public static UUID fileToUuid(String name) {
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            // filename does not confirm with convention; ignore
        }
        return null;
    }

    private void refreshData() {
        File[] brokerDefinitions = location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                // Only accept if both configuration and metadata files are present
                // This may still be insufficient, as metadata could be incorrect
                String filename = file.getName();
                return !file.isDirectory() 
                    &&  fileToUuid(filename) != null
                    &&  getBrokerXbeanFile(filename) != null;
            }
        });

        brokers = new Brokers();
        for (File def : brokerDefinitions) {
            Broker broker = loadBrokerMetadata(def);
            if (broker == null) {
                LOG.warn("Broker definition parse error ({}). Ignoring...", def.getName());
                continue;
            }

            brokers.getBrokers().add(broker);
        }
    }

    private File getBrokerMetadataFile(String brokerid) {
        File path = new File(location, brokerid);
        return path.exists() && !path.isDirectory() ? path : null;
    }

    private File getBrokerXbeanFile(String brokerid) {
        File path = new File(location, brokerid + "-xbean.xml");
        return path.exists() && !path.isDirectory() ? path : null;
    }

    private Broker loadBrokerMetadata(File metadata) {
        Properties p = new Properties();

        try {
            InputStream in = new FileInputStream(metadata);
            p.load(in);
        } catch (IOException e) {
            return null;
        }

        Broker broker = new Broker();
        broker.setName(p.getProperty("id"));
        if (!metadata.getName().equals(broker.getName())) {
            LOG.warn("Broker definition error in {}. Mismatched id={}. ", metadata.getName(), broker.getName());
            return null;
        }

        return broker;
    }

    private void storeBrokerMetadata(Broker broker) {
        Properties p = new Properties();
        p.setProperty("id", broker.getName());
        p.setProperty("name", broker.getName());

        OutputStream out;
        try {
            out = new FileOutputStream(getBrokerMetadataFile(broker.getName()));
            p.store(out, "# Broker definition for " + broker.getName());
        } catch (IOException e) {
        }
    }

}
