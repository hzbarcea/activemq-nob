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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 */
public class TemplateBrokerXbeanConfigurationGeneratorTest {

    private Map<String, String> propmap = ImmutableMap.of("prop1", "value1", "prop2", "value2");

    @Test
    public void testGenerateXbeanConfigurationFile() throws Exception {
        TemplateBrokerXbeanConfigurationGenerator generator = new TemplateBrokerXbeanConfigurationGenerator();
        generator.setSourceTemplateFilename("test-template.vm");
        String result = generator.generateXbeanConfigurationFile(propmap);
        assertEquals("Property 1 equals value1, while Property2 equals value2\n", result);
    }

}