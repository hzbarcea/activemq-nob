/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.File;
import java.io.FileFilter;

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
    private final File rootFolder;
    private Brokers brokers;

    public SupervisorService(File rootFolder) {
        this.rootFolder = rootFolder;
        updateBrokerList();
    }

    private void updateBrokerList() {
        File[] brokerFolders = rootFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                File configFile = new File(file.getPath() + File.separatorChar + "activemq.xml");
                return file.isDirectory() && configFile.exists();
            }
        });

        brokers = new Brokers();
        for (File brokerFolder : brokerFolders) {
            Broker broker = new Broker();
            broker.setName(brokerFolder.getName());
            brokers.getBrokers().add(broker);
        }
    }

    @Override
    public Brokers showBrokers() {
        return brokers;
    }

    @Override
    public Broker showBroker(String brokerid) {
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
        File configFile = new File(getBrokerFolder(brokerid) + File.separatorChar + "activemq.xml");
        if (configFile.exists()) {
            return Response.status(200)
                    .type(MediaType.APPLICATION_XML)
                    .entity(configFile)
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private String getBrokerFolder(String brokerid) {
        return rootFolder.getPath() + File.separatorChar + brokerid;
    }

}
