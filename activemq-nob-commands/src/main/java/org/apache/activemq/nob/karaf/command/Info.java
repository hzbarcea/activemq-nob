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

import java.io.InputStream;
import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationServerPersistenceApi;
import org.apache.cxf.helpers.IOUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "nob", name = "info", description = "Provides details about a broker")
@Service
public class Info implements Action {

    @Option(name = "-b", aliases = {"--broker"}, required = true, multiValued = false, description = "Broker ID")
    private String brokerId;

    @Reference
    BrokerConfigurationServerPersistenceApi serverPersistenceApi;

    @Override
    public Object execute() throws Exception {
        Broker broker = this.serverPersistenceApi.lookupBroker(brokerId);
        if (broker == null) {
            throw new IllegalArgumentException("Broker " + brokerId + " not available");
        }
        System.out.println("ID: " + broker.getId());
        System.out.println("Name: " + broker.getName());
        System.out.println("Status: " + broker.getStatus());
        System.out.println("URL: " + broker.getBrokerUrl());
        System.out.println("Configuration mdate: " + broker.getLastModifiedXbean().getTime());
        System.out.println("Custom properties:");

        Iterable<String> propertyKeys = this.serverPersistenceApi.listProperties(brokerId);
        for (String key : propertyKeys) {
            InputStream contents = this.serverPersistenceApi.getProperty(brokerId, key);
            System.out.println(key + ":");
            System.out.println("---------------------------------------------");
            IOUtils.copy(contents, System.out);
            System.out.println("---------------------------------------------");
        }

        return null;
    }

}
