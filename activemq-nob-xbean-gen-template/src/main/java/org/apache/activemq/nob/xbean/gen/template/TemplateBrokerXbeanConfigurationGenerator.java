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
package org.apache.activemq.nob.xbean.gen.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import org.apache.activemq.nob.xbean.gen.api.BrokerConfigGeneratorException;
import org.apache.activemq.nob.xbean.gen.api.BrokerXbeanConfigurationGeneratorApi;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * xbean generation via template engine (e.g. Velocity)
 */
public class TemplateBrokerXbeanConfigurationGenerator implements BrokerXbeanConfigurationGeneratorApi {

    private final VelocityEngine ve;
    private String sourceTemplateFilename;

    public TemplateBrokerXbeanConfigurationGenerator() throws IOException {
        ve = new VelocityEngine();
        Properties velocityProps = new Properties();
        velocityProps.put(RuntimeConstants.RESOURCE_LOADER, "file, classpath");
        velocityProps.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getCanonicalName());
        ve.init(velocityProps);
    }

    public void setSourceTemplateFilename(String sourceTemplateFilename) {
        this.sourceTemplateFilename = sourceTemplateFilename;
    }

    @Override
    public String generateXbeanConfigurationFile(Map<String, String> configProperties) throws BrokerConfigGeneratorException {
        //add variables to be replaced (e.g. brokerName)
        VelocityContext context = new VelocityContext();
        for (Map.Entry<String, String> entry : configProperties.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }

        try {
            //process input=template
            Template template = ve.getTemplate(sourceTemplateFilename);
            StringWriter sw = new StringWriter();
            template.merge(context, sw);

            //return output=config
            return sw.toString();
        } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException e) {
            throw new TemplateBrokerXbeanConfigurationGeneratorException("Error while generating broker config file from template", e);
        }
    }
}