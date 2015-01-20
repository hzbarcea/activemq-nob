/**
 */
package org.apache.activemq.nob.supervisor;

import java.io.File;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class FileSystemStorageTest {
	private static File nobData;

    @BeforeClass
    public static void initData() {
        String prop = System.getProperty("NOB_DATA");
        if (prop == null) {
             prop = "target/nob";
             System.setProperty("NOB_DATA", prop);
        }
        nobData = new File(prop);
    }

    @AfterClass
    public static void cleanupData() {
        // TODO: may want to leave the $NOB_DATA directory clean after testing
    }

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

        Response answer = supervisor.createBroker();
        String id = (String)answer.getEntity();
        Assert.assertTrue(new File(nobData, id).exists());

        supervisor.deleteBroker(id);
        Assert.assertFalse(new File(nobData, id).exists());
    }


}
