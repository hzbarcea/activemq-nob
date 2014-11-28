/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.api.Brokers;
import org.apache.activemq.nob.api.Supervisor;


/**
 * JAX-RS ControlCenter root resource
 */
public class SupervisorService implements Supervisor {
    private static String DEFAULT_BROKER_CONFIG = "";
    static {
        InputStream is = Supervisor.class.getResourceAsStream("/META-INF/activemq-default.xml");
        if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer content = new StringBuffer();
            String line = null;
            try {
				while ((line = br.readLine()) != null) {
				    content.append(line);
				}
	            br.close();
	            DEFAULT_BROKER_CONFIG = content.toString();
			} catch (IOException e) {
				// ignore
			}
        }
    }

    public Brokers showBrokers() {
		Broker broker;
		Brokers answer = new Brokers();

		broker = new Broker();
		answer.getBrokers().add(broker);
		broker = new Broker();
		answer.getBrokers().add(broker);

		return answer;
	}

	public Broker showBroker(String brokerid) {
		Broker answer = new Broker();
		return answer;
	}

	public void updateBroker(String brokerid, Broker brokertype) {
		// TODO Auto-generated method stub
		
	}

	public void deleteBroker(String brokerid) {
		// TODO Auto-generated method stub
		
	}

	public Response getBrokerConfig(String brokerid) {
	    return Response.status(200).type(MediaType.APPLICATION_XML).entity(DEFAULT_BROKER_CONFIG).build();
	}

}
