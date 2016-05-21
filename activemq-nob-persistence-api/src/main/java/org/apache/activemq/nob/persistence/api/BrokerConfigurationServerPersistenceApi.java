package org.apache.activemq.nob.persistence.api;

import java.io.InputStream;
import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;

import java.util.List;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigNotFoundException;

/**
 * Interface for implementations of persistence for a broker configuration
 * server. Only read-only operations are placed here.
 *
 * Created by art on 2/19/15.
 */
public interface BrokerConfigurationServerPersistenceApi {

    void init() throws BrokerConfigException;

    /**
     * Retrieve the list of known brokers to the persistence layer.
     *
     * @return list of known brokers.
     * @throws org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException
     */
    List<Broker> retrieveBrokerList() throws BrokerConfigPersistenceException;

    /**
     * Retrieve the metadata for the broker with the given ID.
     *
     * @param brokerId ID of the broker to lookup.
     * @return metadata for the matching broker if found; null otherwise.
     */
    Broker lookupBroker(String brokerId);

    /**
     * List property keys associated with a broker.
     *
     * @param brokerId ID of the broker to lookup.
     * @return List of property keys.
     */
    List<String> listProperties(String brokerId) throws BrokerConfigNotFoundException, BrokerConfigPersistenceException;

    /**
     * Look-up a broker property by key
     *
     * @param brokerId ID of the broker to lookup.
     * @param key Property key.
     * @return Content of the property, or null if no such property for the broker.
     */
    InputStream getProperty(String brokerId, String key) throws BrokerConfigNotFoundException, BrokerConfigPersistenceException;

    /**
     * Retrieve the broker configuration in the XBean (XML-based) format.
     *
     * @param brokerId ID of the broker to lookup.
     * @return the content of the broker xbean configuration.
     * @throws org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException
     */
    XBeanContent getBrokerXbeanConfiguration(String brokerId) throws BrokerConfigPersistenceException;

    /**
     * Retrieve an XML configuration file for the broker.
     *
     * @param brokerId ID of the broker for which to retrieve the file.
     * @param configName name of the configuration to retrieve.
     * @return content of the configuration file.
     * @throws BrokerConfigPersistenceException
     */
    XMLConfigContent getBrokerXmlConfigFile(String brokerId, String configName) throws BrokerConfigPersistenceException;
}
