package org.apache.activemq.nob.persistence.api;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigNotFoundException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;

import java.io.InputStream;

/**
 * Write operations for a broker configuration persistence store.
 *
 * Created by art on 2/19/15.
 */
public interface BrokerConfigurationUpdatePersistenceApi {

    void init() throws BrokerConfigException;

    /**
     * Create a new broker with the given details in the persistent store.
     *
     * @param newBroker information for the new broker.
     * @param xbeanContent
     * @throws BrokerConfigPersistenceException on errors accessing persistence
     * or creating the broker
     */
    void createNewBroker(Broker newBroker, InputStream xbeanContent) throws BrokerConfigPersistenceException;

    /**
     * Update the information for an existing broker.
     *
     * @param updateBroker the broker information to update in the persistent
     * store.
     * @throws BrokerConfigPersistenceException on errors accessing persistence
     * or updating the broker
     * @throws BrokerConfigNotFoundException if the broker does not exist
     *
     */
    void updateBroker(Broker updateBroker) throws BrokerConfigPersistenceException, BrokerConfigNotFoundException;

    /**
     * Set a broker property
     *
     * @param brokerId ID of the broker to lookup.
     * @param key Property key.
     * @param content Property content.
     * @throws BrokerConfigPersistenceException on errors accessing persistence
     * or updating the broker
     * @throws BrokerConfigNotFoundException if the broker does not exist
     */
    void setProperty(String brokerId, String key, InputStream content) throws BrokerConfigNotFoundException, BrokerConfigPersistenceException;    
    
    /**
     * Remove a broker property
     *
     * @param brokerId ID of the broker to lookup.
     * @param key Property key.
     * @throws BrokerConfigPersistenceException on errors accessing persistence
     * or updating the broker
     * @throws BrokerConfigNotFoundException if the broker does not exist
     */
    void unsetProperty(String brokerId, String key) throws BrokerConfigNotFoundException, BrokerConfigPersistenceException;

    /**
     * Write the XBean configuration for the broker with the given identifier.
     *
     * @param brokerId ID of the broker for which to write the configuration.
     * @param xbeanContent source of the configuration of the broker in xbean (XML) format.
     * @throws org.apache.activemq.nob.persistence.api.exception.BrokerConfigException
     */
    void writeBrokerXbeanConfig(String brokerId, InputStream xbeanContent) throws BrokerConfigException;

    /**
     * Remove the broker with the given ID.
     *
     * @param brokerId ID of the broker to remove.
     */
    void removeBroker(String brokerId) throws BrokerConfigPersistenceException;
}
