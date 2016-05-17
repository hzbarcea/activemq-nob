/**
 */
package org.apache.activemq.nob.supervisor;

import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.activemq.nob.ActiveMQNobConstants;
import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.api.Brokers;
import org.apache.activemq.nob.api.PropertyKeyList;
import org.apache.activemq.nob.api.Supervisor;
import org.apache.activemq.nob.deployment.api.BrokerDeploymentApi;
import org.apache.activemq.nob.deployment.api.exception.BrokerDeploymentException;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationServerPersistenceApi;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationUpdatePersistenceApi;
import org.apache.activemq.nob.persistence.api.XBeanContent;
import org.apache.activemq.nob.persistence.api.XMLConfigContent;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;
import org.apache.activemq.nob.xbean.gen.api.BrokerConfigGeneratorException;
import org.apache.activemq.nob.xbean.gen.api.BrokerXbeanConfigurationGeneratorApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS ControlCenter root resource
 */
public class SupervisorService implements Supervisor {
    private static final Logger LOG = LoggerFactory.getLogger(SupervisorService.class);

    private BrokerXbeanConfigurationGeneratorApi xbeanGenerator;
    private BrokerConfigurationServerPersistenceApi serverPersistenceApi;
    private BrokerConfigurationUpdatePersistenceApi updatePersistenceApi;
    private BrokerDeploymentApi deploymentApi;

    @Context
    private UriInfo ui;

    public SupervisorService() {
    }

    @Context
    public void setUriInfo(UriInfo ui) {
        this.ui = ui;
    }

    public UriInfo getUriInfo() {
        return this.ui;
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

    public void setDeploymentApi(BrokerDeploymentApi deploymentApi) {
        this.deploymentApi = deploymentApi;
    }

    public BrokerDeploymentApi getDeploymentApi() {
        return deploymentApi;
    }

    public void setXbeanGenerator(BrokerXbeanConfigurationGeneratorApi xbeanGenerator) {
        this.xbeanGenerator = xbeanGenerator;
    }

    public BrokerXbeanConfigurationGeneratorApi getXbeanGenerator() {
        return xbeanGenerator;
    }

    public void init() throws RuntimeException {
    }

    // REST ENDPOINT
    @Override
    public PropertyKeyList listProperties(String brokerid) {
        PropertyKeyList result = new PropertyKeyList();
        try {
            List<String> props = this.serverPersistenceApi.listProperties(brokerid);
            if (props != null) {
                for (String key : props) {
                    result.getKeies().add(key);
                }
            }
        } catch (BrokerConfigException ex) {
            LOG.warn("failed to read properties for broker: brokerId={}", brokerid, ex);
        }
        return result;
    }

    // REST ENDPOINT
    @Override
    public Response getProperty(String brokerid, String key) {
        try {
            InputStream value = this.serverPersistenceApi.getProperty(brokerid, key);
            return value == null
                    ? Response.status(Response.Status.NOT_FOUND).build()
                    : Response.ok().entity(value).build();
        } catch (BrokerConfigException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // REST ENDPOINT
    @Override
    public void setProperty(String brokerid, String key, String value) {
        try {
            InputStream contents = makeStringInputStream(value);
            this.updatePersistenceApi.setProperty(brokerid, key, contents);
        } catch (BrokerConfigException ex) {
            LOG.warn("failed to update properties for broker: brokerId={}", brokerid, ex);
            throw new RuntimeException("failed to update broker properties file", ex);
        }
    }

    // REST ENDPOINT
    @Override
    public void unsetProperty(String brokerid, String key) {
        try {
            this.updatePersistenceApi.unsetProperty(brokerid, key);
        } catch (BrokerConfigException ex) {
            LOG.warn("failed to update properties for broker: brokerId={}", brokerid, ex);
            throw new RuntimeException("failed to update broker properties file", ex);
        }
    }

    // REST ENDPOINT
    @Override
    public Response createBroker() {
        Broker broker = createBroker(UUID.randomUUID());
        try {
            String xbeanContent = generateBrokerXbean(broker);
            InputStream xbeanContentSource = makeStringInputStream(xbeanContent);
            this.updatePersistenceApi.createNewBroker(broker, xbeanContentSource);
        } catch (BrokerConfigGeneratorException | BrokerConfigPersistenceException ex) {
            this.LOG.error("failed create a new broker", ex);
            throw new RuntimeException("failed to create a new broker", ex);
        }

        return Response.ok().type(MediaType.APPLICATION_XML).entity(broker.getName()).build();
    }

    // REST ENDPOINT
    @Override
    public Brokers getBrokers(String filter) {
        try {
            List<Broker> persistenceBrokerList = this.serverPersistenceApi.retrieveBrokerList();
            Brokers result = new Brokers();
            // TODO: deal with the filter
            result.getBrokers().addAll(persistenceBrokerList);

            return result;
        } catch (BrokerConfigPersistenceException exc) {
            this.LOG.error("failed to retrieve broker list", exc);
            throw new RuntimeException("failed to retrieve the broker list", exc);
        }
    }

    // REST ENDPOINT
    @Override
    public Broker getBroker(String brokerid) {
        return this.serverPersistenceApi.lookupBroker(brokerid);
    }

    // REST ENDPOINT
    @Override
    public void updateBroker(String brokerid, Broker brokertype) {
        try {
            this.updatePersistenceApi.updateBroker(brokertype);
        } catch (BrokerConfigException ex) {
            LOG.error("failed to update broker", ex);
            throw new RuntimeException("failed to update broker", ex);
        }
    }

    // REST ENDPOINT
    @Override
    public void deleteBroker(String brokerid) {
        try {
            this.updatePersistenceApi.removeBroker(brokerid);
        } catch (BrokerConfigPersistenceException exc) {
            this.LOG.error("failed to retrieve broker xbean config", exc);
            throw new RuntimeException("failed to retrieve broker xbean config", exc);
        }
    }

    // REST ENDPOINT
    @Override
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
    @Override
    public void setBrokerStatus(String brokerid, String status) {
        try {
            Broker broker = this.serverPersistenceApi.lookupBroker(brokerid);
            broker.setStatus(status);
            this.updatePersistenceApi.updateBroker(broker);
        } catch (BrokerConfigException ex) {
            LOG.warn("failed to update xbean config for broker: brokerId={}", brokerid, ex);
            throw new RuntimeException("failed to update broker file", ex);
        }
    }

    // REST ENDPOINT
    @Override
    public Response getBrokerStatus(String brokerid) {
        Broker broker = this.serverPersistenceApi.lookupBroker(brokerid);
        return broker != null ?
            Response.ok().type(MediaType.TEXT_PLAIN).entity(broker.getStatus()).build() :
            Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response getBrokerConfigXmlFile (String brokerid, String configName) {
        try {
            XMLConfigContent configContent = this.serverPersistenceApi.getBrokerXmlConfigFile(brokerid, configName);
            return (configContent != null) ?
                    Response.ok()
                            .type(MediaType.APPLICATION_XML)
                            .entity(configContent.getContent())
                            .lastModified(new Date(configContent.getLastModified()))
                            .build()
                    :
                    Response.status(Response.Status.NOT_FOUND).build();
        } catch ( BrokerConfigPersistenceException exc ) {
            this.LOG.error("failed to retrieve broker xml config for {}", configName, exc);
            throw new RuntimeException("failed to retrieve broker xml config for " + configName, exc);
        }
    }

    private Broker createBroker(UUID uuid) {
        return createBroker(uuid.toString());
    }

    private Broker createBroker(String id) {
        Broker broker = new Broker();
        broker.setId(id);
        broker.setName(id);
        broker.setStatus(ActiveMQNobConstants.STATUS_NEW);
        return broker;
    }

    private String generateBrokerXbean(Broker broker) throws BrokerConfigGeneratorException {
        Map<String, String> configProperties = Maps.newHashMap();
        configProperties.put("brokerName", broker.getName());
        return xbeanGenerator.generateXbeanConfigurationFile(configProperties);
    }

    private InputStream makeStringInputStream(String value) {
        try {
            ByteArrayInputStream result = new ByteArrayInputStream(value.getBytes("UTF-8"));
            return result;
        } catch (UnsupportedEncodingException ueExc) {
            LOG.error("UTF-8 unsupported");
            throw new RuntimeException("UTF-8 unsupported");
        }
    }

    @Override
    public Response deployBroker(String brokerid) {
        Broker broker = this.serverPersistenceApi.lookupBroker(brokerid);
        if (broker == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            deploymentApi.deploy(broker);
            return Response.ok().build();
        } catch (BrokerDeploymentException ex) {
            LOG.error("Could not deploy broker {}", brokerid, ex);
            throw new RuntimeException("failed to deploy broker", ex);
        }
    }

    @Override
    public Response undeployBroker(String brokerid) {
        Broker broker = this.serverPersistenceApi.lookupBroker(brokerid);
        if (broker == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            deploymentApi.undeploy(broker);
            return Response.ok().build();
        } catch (BrokerDeploymentException ex) {
            LOG.error("Could not undeploy broker {}", brokerid, ex);
            throw new RuntimeException("failed to undeploy broker", ex);
        }
    }

}
