/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.api.Brokers;
import org.apache.activemq.nob.api.Supervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

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
        storeBrokerXbean(broker, generateBrokerXbean(broker));

        //  TODO: setting metadata should probably automatically trigger persistent storage of value
        brokers.put(broker.getId(), broker);
        // no need to add the default name to aliases

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

    @Override
    public void updateBroker(String brokerid, Broker brokerPatch) {
        Broker broker = brokers.get(brokerid);
        boolean changed = false;
        if (broker == null) {
            broker = createBroker(brokerid);
            changed = true;
        }

        if (brokerPatch.getName() != null && !brokerPatch.getName().equals(broker.getName())) {
            broker.setName(brokerPatch.getName());
            changed = true;
        }
        if (brokerPatch.getStatus() != null && !brokerPatch.getStatus().equals(broker.getStatus())) {
            broker.setStatus(brokerPatch.getStatus());
            changed = true;
        }
        // won't patch broker.id
        // won't patch broker.lastModifiedXbean

        if (changed) {
            //  TODO: setting metadata should probably automatically trigger persistent storage of value
            storeBrokerMetadata(broker);
            brokers.put(broker.getId(), broker);
        }
    }

    public void deleteBroker(String brokerid) {
        brokers.remove(brokerid);

        // Now remove files associated with the broker removed...
        deleteBrokerDataFile(getBrokerMetadataFile(brokerid));
        deleteBrokerDataFile(getBrokerXbeanFile(brokerid));
    }

    public Response getBrokerXbeanConfig(String brokerid) {
        File xbeanFile = getBrokerXbeanFile(brokerid);
        return (xbeanFile != null)
                ? Response.ok()
                .type(MediaType.APPLICATION_XML)
                .entity(xbeanFile)
                .lastModified(new Date(xbeanFile.lastModified())).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public void putBrokerXbeanConfig(String brokerid, String xbeanContent) {
        Broker broker = brokers.get(brokerid);
        if (broker == null) {
            broker = createBroker(brokerid);
            storeBrokerMetadata(broker);
        }

        storeBrokerXbean(broker, xbeanContent);

        //  TODO: setting metadata should probably automatically trigger persistent storage of value
        brokers.put(broker.getId(), broker);
    }

    public Response getBrokerStatus(String brokerid) {
        Broker broker = brokers.get(brokerid);
        return broker != null
                ? Response.ok().type(MediaType.TEXT_PLAIN).entity(broker.getStatus()).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    private Broker createBroker(UUID uuid) {
        return createBroker(uuid.toString());
    }

    private Broker createBroker(String id) {
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
                        && fileToUuid(filename) != null
                        && getBrokerXbeanFile(filename) != null;
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
        return path.exists() && path.isDirectory() ? null : path;
    }

    private File getBrokerXbeanFile(String brokerid) {
        File path = new File(location, brokerid + "-xbean.xml");
        return path.exists() && path.isDirectory() ? null : path;
    }

    private void deleteBrokerDataFile(File data) {
        if (data != null && data.exists()) {
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

        // Right now we don't store the lastModified timestamp, we retrive from fs
        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(getBrokerXbeanFile(broker.getId()).lastModified());
        broker.setLastModifiedXbean(lastModified);

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

        try {
            OutputStream out = new FileOutputStream(getBrokerMetadataFile(broker.getName()));
            p.store(out, "# Broker definition for " + broker.getName());
        } catch (IOException e) {
            LOG.info("Failed to store broker metadata for '{}': {}", broker.getName(), e.getMessage());
        }
    }

    private void storeBrokerXbean(Broker broker, String xbean) {
        OutputStream out;
        try {
            out = new FileOutputStream(getBrokerXbeanFile(broker.getName()));
            out.write(xbean.getBytes());
            out.close();
        } catch (IOException e) {
            // LOG ?
            return;
        }

        // If successful let's update the broker metadata
        broker.setLastModifiedXbean(Calendar.getInstance());
    }

    private String generateBrokerXbean(Broker broker) {
        String xbean = getXbeanConfigurationTemplate();
        return xbean.replaceAll("\\$brokerName", broker.getName());
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
        String envData = System.getProperty("NOB_DATA");
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

    public static final String getXbeanConfigurationTemplate() {
        try (
                final InputStream xbean = SupervisorService.class.getResourceAsStream("/META-INF/activemq-default.xml");
                final InputStreamReader in = new InputStreamReader(xbean)) {
            return CharStreams.toString(in);
        } catch (IOException e) {
            LOG.error("Could not read the default xbean configuration template");
        }
        return null;
    }

}
