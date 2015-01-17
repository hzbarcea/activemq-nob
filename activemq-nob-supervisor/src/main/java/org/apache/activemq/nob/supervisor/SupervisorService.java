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
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private File location; // Broker data store; TODO: create an abstraction for storage
    private Map<String, Broker> brokers = new ConcurrentHashMap<String, Broker>();
    private Map<String, String> aliases = new ConcurrentHashMap<String, String>();

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
        Broker broker = createBroker(UUID.randomUUID());
        storeBrokerMetadata(broker);
        // by default id == name, no impact on aliases
        brokers.put(broker.getId(), broker);
        return Response.ok().type(MediaType.APPLICATION_XML).entity(broker.getName()).build();
    }

    public Brokers getBrokers(String filter) {
        Brokers result = new Brokers();
        // TODO: deal with the filter
        result.getBrokers().addAll(brokers.values());
        return result;
    }

    @Override
    public Broker getBroker(String brokerid) {
        return brokers.get(brokerid);
    }

    public void updateBroker(String brokerid, Broker brokertype) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void deleteBroker(String brokerid) {
        brokers.remove(brokerid);

        // Now remove files associated with the broker removed...
        deleteBrokerDataFile(getBrokerMetadataFile(brokerid));
        deleteBrokerDataFile(getBrokerXbeanFile(brokerid));
    }

    public Response getBrokerXbeanConfig(String brokerid) {
        File xbeanFile = getBrokerXbeanFile(brokerid);
        return (xbeanFile != null) ?
            Response.ok().type(MediaType.APPLICATION_XML).entity(xbeanFile).build() :
            Response.status(Response.Status.NOT_FOUND).build();
            // TODO: add lastUpdated header !
    }

    public Response getBrokerStatus(String brokerid) {
        Broker broker = brokers.get(brokerid);
        return broker != null ?
            Response.ok().type(MediaType.TEXT_PLAIN).entity(broker.getStatus()).build() :
            Response.status(Response.Status.NOT_FOUND).build();
    }


    private Broker createBroker(UUID uuid) {
        String id = uuid.toString();
        Broker broker = new Broker();
        broker.setId(id);
        broker.setName(id);
        broker.setStatus(SupervisorConstants.STATUS_NEW);
        return broker;
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

        brokers.clear();
        for (File def : brokerDefinitions) {
            Broker broker = loadBrokerMetadata(def);
            if (broker == null) {
                LOG.warn("Broker definition parse error ({}). Ignoring...", def.getName());
                continue;
            }

            String id = broker.getId();
            Broker old = brokers.put(id, broker);
            if (!id.equals(broker.getName())) {
                aliases.put(broker.getName(), id);
            }
            if (old != null) {
                // We avoided synchronizing using a ConcurrentHashMap, but
                // if we replaced an existing value, there is a logic failure somewhere
                LOG.warn("NOB - broker {} already exists. Data may be corrupted.");
            }
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

    private void deleteBrokerDataFile(File data) {
        if (data != null) {
            LOG.info("Deleting broker data: {}", data.getName());
            data.delete();
        }
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
        broker.setId(p.getProperty("id"));
        broker.setName(p.getProperty("name"));
        broker.setStatus(p.getProperty("status"));
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
        p.setProperty("status", broker.getStatus());

        OutputStream out;
        try {
            out = new FileOutputStream(getBrokerMetadataFile(broker.getName()));
            p.store(out, "# Broker definition for " + broker.getName());
        } catch (IOException e) {
        }
    }


    public static UUID fileToUuid(String name) {
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            // filename does not confirm with convention; ignore
        }
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

}
