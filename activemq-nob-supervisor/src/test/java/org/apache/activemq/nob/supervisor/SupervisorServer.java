/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;


/**
 * JAX-RS Supervisor root resource
 */
public class SupervisorServer {
    private static Server server;

    protected SupervisorServer(String location) throws Exception {
        SupervisorApp application = new SupervisorApp(location);
        RuntimeDelegate delegate = RuntimeDelegate.getInstance();

        Map<Object, Object> mappings = new HashMap<Object, Object>();
        mappings.put("json", "application/json");
        mappings.put("xml", "application/xml");
        
        JAXRSServerFactoryBean bean = delegate.createEndpoint(application, JAXRSServerFactoryBean.class);
        bean.setAddress("http://0.0.0.0:9000/services" + bean.getAddress());
        bean.setExtensionMappings(mappings);
        System.out.println("Available at: " + bean.getAddress());
        server = bean.create();
        server.start();
    }

    public static void main(String args[]) throws Exception {
        new SupervisorServer("src/test/resources/nob");
        System.out.println("Server ready...");

        Thread.sleep(125 * 60 * 1000);
        System.out.println("Server exiting");
        server.stop();
        server.destroy();
        System.exit(0);
    }
}
