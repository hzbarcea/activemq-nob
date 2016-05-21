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

import org.apache.activemq.nob.persistence.api.BrokerConfigurationUpdatePersistenceApi;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "nob", name = "delete", description = "Deletes a broker from the nob")
@Service
public class Delete implements Action {

    @Option(name = "-b", aliases = {"--broker"}, required = true, multiValued = false, description = "Broker ID")
    private String brokerId;

    @Reference
    BrokerConfigurationUpdatePersistenceApi updatePersistenceApi;

    @Override
    public Object execute() throws Exception {
        this.updatePersistenceApi.removeBroker(brokerId);
        System.out.println("Removed broker " + brokerId);
        return null;
    }

}
