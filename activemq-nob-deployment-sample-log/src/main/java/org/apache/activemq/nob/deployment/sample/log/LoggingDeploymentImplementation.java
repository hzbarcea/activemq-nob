/*
 * Copyright 2015 The Apache Software Foundation.
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
package org.apache.activemq.nob.deployment.sample.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.deployment.api.BrokerDeploymentApi;
import org.apache.activemq.nob.deployment.api.exception.BrokerDeploymentException;

/**
 * Sample broker deployment implementation.
 *
 * @author Ciprian Ciubotariu <cheepeero@gmx.net>
 */
public class LoggingDeploymentImplementation implements BrokerDeploymentApi {

    public static final Logger LOG = LoggerFactory.getLogger(LoggingDeploymentImplementation.class);

    @Override
    public void init() {
        LOG.info("initializing Logging deployer");
    }

    @Override
    public void deploy(Broker broker) throws BrokerDeploymentException {
        LOG.info("broker {} ({}): deployed", broker.getName(), broker.getStatus());
    }

    @Override
    public void undeploy(Broker broker) throws BrokerDeploymentException {
        LOG.info("broker {} ({}): undeployed", broker.getName(), broker.getStatus());
    }

}
