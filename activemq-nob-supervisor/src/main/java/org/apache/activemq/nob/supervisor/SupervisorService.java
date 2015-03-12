/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.activemq.nob.ActiveMQNobConstants;
import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.api.Brokers;
import org.apache.activemq.nob.api.Supervisor;
import org.apache.activemq.nob.filestore.DefaultFileStorePersistenceAdapter;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationServerPersistenceApi;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationUpdatePersistenceApi;
import org.apache.activemq.nob.persistence.api.XBeanContent;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;


/**
 * JAX-RS ControlCenter root resource
 */
public class SupervisorService implements Supervisor {
    private static final Logger LOG = LoggerFactory.getLogger(SupervisorService.class);

    private File location; // Broker data store; TODO: create an abstraction for storage

    private BrokerConfigurationServerPersistenceApi serverPersistenceApi;
    private BrokerConfigurationUpdatePersistenceApi updatePersistenceApi;

    public SupervisorService() {
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public BrokerConfigurationServerPersistenceApi getServerPersistenceApi() {
        return serverPersistenceApi;
    }

    public void setServerPersistenceApi(BrokerConfigurationServerPersistenceApi serverPersistenceApi) {
        this.serverPersistenceApi = serverPersistenceApi;
    }

    public BrokerConfigurationUpdatePersistenceApi getUpdatePersistenceApi() {
        return updatePersistenceApi;
    }

    public void setUpdatePersistenceApi(BrokerConfigurationUpdatePersistenceApi updatePersistenceApi) {
        this.updatePersistenceApi = updatePersistenceApi;
    }

    public void init() throws RuntimeException {
        if ( ( this.serverPersistenceApi == null ) || ( this.updatePersistenceApi == null ) ) {
            configureDefaultPersistence();
        }

        this.serverPersistenceApi.init();
        this.updatePersistenceApi.init();
    }

    // REST ENDPOINT
    public Response createBroker() {
        Broker broker = createBroker(UUID.randomUUID());
        String xbeanContent = generateBrokerXbean(broker);

        try {
            InputStream xbeanContentSource = makeStringInputStream(xbeanContent);
            this.updatePersistenceApi.createNewBroker(broker, xbeanContentSource);
        } catch (BrokerConfigPersistenceException persistenceExc) {
            this.LOG.error("failed create a new broker", persistenceExc);
            throw new RuntimeException("failed to persist the broker", persistenceExc);
        }

        return Response.ok().type(MediaType.APPLICATION_XML).entity(broker.getName()).build();
    }

    // REST ENDPOINT
    public Brokers getBrokers(String filter) {
        try {
            List<Broker> persistenceBrokerList = this.serverPersistenceApi.retrieveBrokerList();
            Brokers result = new Brokers();
            // TODO: deal with the filter
            result.getBrokers().addAll(persistenceBrokerList);

            return result;
        } catch ( BrokerConfigPersistenceException exc ) {
            this.LOG.error("failed to retrieve broker list", exc);
            throw new RuntimeException("failed to retrieve the broker list", exc);
        }
    }

    // REST ENDPOINT
    @Override
    public Broker getBroker(String brokerid) {
        return  this.serverPersistenceApi.lookupBroker(brokerid);
    }

    // REST ENDPOINT
    public void updateBroker(String brokerid, Broker brokertype) {
        try {
            this.updatePersistenceApi.updateBroker(brokertype);
        } catch (BrokerConfigException ex) {
            LOG.error("failed to update broker", ex);
            throw new RuntimeException("failed to update broker", ex);
        }
    }

    // REST ENDPOINT
    public void deleteBroker(String brokerid) {
        try {
            this.updatePersistenceApi.removeBroker(brokerid);
        } catch ( BrokerConfigPersistenceException exc ) {
            this.LOG.error("failed to retrieve broker xbean config", exc);
            throw new RuntimeException("failed to retrieve broker xbean config", exc);
        }
    }

    // REST ENDPOINT
    public Response getBrokerXbeanConfig(String brokerid) {
        try {
            XBeanContent xBeanContent = this.serverPersistenceApi.getBrokerXbeanConfiguration(brokerid);
            return (xBeanContent != null) ?
                    Response.ok()
                            .type(MediaType.APPLICATION_XML)
                            .entity(xBeanContent.getContent())
                            .lastModified(new Date(xBeanContent.getLastModified())).build() :
                    Response.status(Response.Status.NOT_FOUND).build();
        } catch ( BrokerConfigPersistenceException exc ) {
            this.LOG.error("failed to retrieve broker xbean config", exc);
            throw new RuntimeException("failed to retrieve broker xbean config", exc);
        }
    }

    // REST ENDPOINT
    @Override
    public void putBrokerXbeanConfig(String brokerid, String xbeanContent) {
        try {
            InputStream xbeanContentSource = makeStringInputStream(xbeanContent);

            this.updatePersistenceApi.writeBrokerXbeanConfig(brokerid, xbeanContentSource);
        } catch (BrokerConfigException bcExc) {
            this.LOG.warn("failed to update xbean config for broker: brokerId={}", brokerid, bcExc);
            throw new RuntimeException("failed to update broker xbean file", bcExc);
        }
    }

    // REST ENDPOINT
    public Response getBrokerStatus(String brokerid) {
        Broker broker = this.serverPersistenceApi.lookupBroker(brokerid);
        return broker != null ?
            Response.ok().type(MediaType.TEXT_PLAIN).entity(broker.getStatus()).build() :
            Response.status(Response.Status.NOT_FOUND).build();
    }

    protected void configureDefaultPersistence () {
        if (location == null) {
            location = getDataLocation();
            if (location == null) {
                throw new RuntimeException("Cannot access NOB data");
            }
        }

        DefaultFileStorePersistenceAdapter result = new DefaultFileStorePersistenceAdapter(location);

        LOG.info("Using NOB data at {}", location.getAbsolutePath());

        result.setLocation(location);

        if ( this.serverPersistenceApi == null ) {
            this.serverPersistenceApi = result;
        }

        if ( this.updatePersistenceApi == null ) {
            this.updatePersistenceApi = result;
        }
    }

    private Broker createBroker(UUID uuid) {
        return  createBroker(uuid.toString());
    }

    private Broker createBroker(String id) {
        Broker broker = new Broker();
        broker.setId(id);
        broker.setName(id);
        broker.setStatus(ActiveMQNobConstants.STATUS_NEW);
        return broker;
    }

    private String generateBrokerXbean(Broker broker) {
        String xbean = getXbeanConfigurationTemplate();
        return xbean.replaceAll("\\$brokerName", broker.getName());
    }

    private static File getDataLocation() {
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

    private InputStream makeStringInputStream(String value) {
        try {
            ByteArrayInputStream result = new ByteArrayInputStream(value.getBytes("UTF-8"));
            return  result;
        } catch ( UnsupportedEncodingException ueExc ) {
            LOG.error("UTF-8 unsupported");
            throw new RuntimeException("UTF-8 unsupported");
        }
    }

    public static final String getXbeanConfigurationTemplate() {
        try (
		    final InputStream xbean = SupervisorService.class.getResourceAsStream( "/META-INF/activemq-default.xml" );
			final InputStreamReader in = new InputStreamReader(xbean)) {
		    return CharStreams.toString(in);
		} catch (IOException e) {
			LOG.error("Could not read the default xbean configuration template");
		}
        return null;
    }

}
