/**
 */
package org.apache.activemq.nob.supervisor;

import org.junit.Assert;
import org.junit.Test;


public class FileSystemStorageTest {

	@Test
    public void testXbeanTemplate() throws Exception {
		String template = SupervisorService.getXbeanConfigurationTemplate();
		Assert.assertNotNull(template);
		Assert.assertTrue(template.indexOf("\"$brokerName\"") > 0);
    }

	@Test
    public void testServiceCreateBroker() throws Exception {
		SupervisorService supervisor = new SupervisorService();
		supervisor.init();

		supervisor.createBroker();
    }


}
