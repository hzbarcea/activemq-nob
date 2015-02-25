package org.apache.activemq.nob.persistence.api;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;

import java.io.InputStream;

/**
 * Write operations for a broker configuration persistence store.
 *
 * Created by art on 2/19/15.
 */
public interface BrokerConfigurationUpdatePersistenceApi {

    void init();

    /**
     * Create a new broker with the given details in the persistent store.
     *
     * @param newBroker information for the new broker.
     * @param xbeanContent
     */
    void createNewBroker(Broker newBroker, InputStream xbeanContent) throws BrokerConfigPersistenceException;

    /**
     * Update the information for an existing broker.
     *
     * @param updateBroker the broker information to update in the persistent store.
     * @throws BrokerConfigPersistenceException on errors accessing persistence or updating the broker
     */
    void updateBroker(Broker updateBroker) throws BrokerConfigException;

    /**
     * Write the XBean configuration for the broker with the given identifier.
     *
     * @param brokerId ID of the broker for which to write the configuration.
     * @param xbeanContent source of the configuration of the broker in xbean (XML) format.
     */
    void writeBrokerXbeanConfig(String brokerId, InputStream xbeanContent) throws BrokerConfigException;

    /**
     * Remove the broker with the given ID.
     *
     * @param brokerId ID of the broker to remove.
     */
    void removeBroker(String brokerId) throws BrokerConfigPersistenceException;
}
