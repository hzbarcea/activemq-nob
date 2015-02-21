package org.apache.activemq.nob.persistence.api;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;

import java.util.List;

/**
 * Interface for implementations of persistence for a broker configuration server.  Only read-only operations are
 * placed here.
 *
 * Created by art on 2/19/15.
 */
public interface BrokerConfigurationServerPersistenceApi {
    /**
     * Retrieve the list of known brokers to the persistence layer.
     *
     * @return list of known brokers.
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
     * Retrieve the broker configuration in the XBean (XML-based) format.
     *
     * @return the content of the broker xbean configuration.
     */
    XBeanContent getBrokerXbeanConfiguration(String brokerId) throws BrokerConfigPersistenceException;
}
