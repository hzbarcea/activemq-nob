/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.File;
import java.io.FileFilter;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.api.Brokers;
import org.apache.activemq.nob.api.Supervisor;

/**
 * JAX-RS ControlCenter root resource
 */
public class SupervisorService implements Supervisor {

    /**
     * Folder where the broker config structure is.
     */
    private File nobHome;
    private Brokers brokers;

    public SupervisorService(String location) {
    	File home = new File(location);
        if (home.exists()) {
        	nobHome = home;
            updateBrokerList();
        }
    }

    private void updateBrokerList() {
        File[] brokerFolders = nobHome.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
            	// Only accept if both configuration and metadata files are present
            	// This may still be insufficient, as metadata could be incorrect
                return file.isDirectory() 
                    && new File(file, "activemq.xml").exists()
                    && new File(file, file.getName() + ".properties").exists();
            }
        });

        brokers = new Brokers();
        for (File brokerFolder : brokerFolders) {
            Broker broker = new Broker();
            broker.setName(brokerFolder.getName());
            brokers.getBrokers().add(broker);
        }
    }

	public Response createBroker() {
        Broker broker = new Broker();
        broker.setName(UUID.randomUUID().toString());
        brokers.getBrokers().add(broker);
		return Response.ok().type(MediaType.APPLICATION_XML).entity(broker.getName()).build();
	}

    @Override
    public Brokers getBrokers() {
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

    @Override
    public void updateBroker(String brokerid, Broker brokertype) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void deleteBroker(String brokerid) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Response getBrokerConfig(String brokerid) {
    	File path = getBrokerConfigPath(brokerid);
        File configFile = path != null ? new File(path, "activemq.xml") : null;
        return (configFile != null && configFile.exists()) ?
            Response.ok().type(MediaType.APPLICATION_XML).entity(configFile).build() :
            Response.status(Response.Status.NOT_FOUND).build();
    }

    private File getBrokerConfigPath(String brokerid) {
    	File path = new File(nobHome, brokerid);
    	return path.exists() ? path : null;
    }

}
