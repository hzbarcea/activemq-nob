/*
 * Copyright 2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.nob.karaf.command;

import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import org.apache.activemq.nob.ActiveMQNobConstants;
import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationUpdatePersistenceApi;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "nob", name = "create", description = "Creates a new broker in the nob")
@Service
public class Create implements Action {

    @Reference
    BrokerConfigurationUpdatePersistenceApi updatePersistenceApi;

    @Override
    public Object execute() throws Exception {
        String brokerId = UUID.randomUUID().toString();

        Broker broker = new Broker();
        broker.setId(brokerId);
        broker.setName(brokerId);
        broker.setStatus(ActiveMQNobConstants.STATUS_NEW);

        String xbeanContent = generateBrokerXbean(broker);
        InputStream xbeanContentSource = makeStringInputStream(xbeanContent);
        this.updatePersistenceApi.createNewBroker(broker, xbeanContentSource);

        System.out.println("Created broker " + broker.getId());

        return null;
    }

    // TODO: code below duplicates SupervisorService implementation
    private String generateBrokerXbean(Broker broker) throws IOException {
        String xbean = getXbeanConfigurationTemplate();
        return xbean.replaceAll("\\$brokerName", broker.getName());
    }

    private InputStream makeStringInputStream(String value) throws IOException {
        ByteArrayInputStream result = new ByteArrayInputStream(value.getBytes("UTF-8"));
        return result;
    }

    public static final String getXbeanConfigurationTemplate() throws IOException {
        // source = 1.5 ... i l
        InputStream xbean = null;
        InputStreamReader in = null;
        try {
            xbean = Create.class.getResourceAsStream("/activemq-default.xml");
            in = new InputStreamReader(xbean);
            return CharStreams.toString(in);
        } finally {
            if (in != null) {
                in.close();
            }
            if (xbean != null) {
                xbean.close();
            }
        }
    }

}

