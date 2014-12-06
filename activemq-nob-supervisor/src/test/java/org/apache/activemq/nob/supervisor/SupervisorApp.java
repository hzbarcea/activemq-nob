/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.cxf.jaxrs.provider.json.JSONProvider;


/*
 * Class that can be used (instead of XML-based configuration) to inform the JAX-RS
 * runtime about the resources and providers it is supposed to deploy.  See the
 * ApplicationServer class for more information.
 */
@ApplicationPath("/nob")
public class SupervisorApp extends Application {

    private final String nobHome;

    public SupervisorApp(String location) {
        nobHome = location;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>();
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> classes = new HashSet<Object>();

        SupervisorService supervisor = new SupervisorService(nobHome);
        classes.add(supervisor);

        // custom providers
        JSONProvider<?> provider = new JSONProvider<Object>();
        provider.setIgnoreNamespaces(true);
        classes.add(provider);

        return classes;
    }

}
