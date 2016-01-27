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
package org.apache.activemq.nob.supervisor;

import java.io.File;
import javax.ws.rs.core.Response;
import org.apache.activemq.nob.filestore.DefaultFileStorePersistenceAdapter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SupervisorIT {

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

//      @AfterClass
//    public static void cleanupData() {
//        nobData.delete(); // TODO: recursively
//    }

    @Test
    public void testServiceCreateBroker() throws Exception {
        SupervisorService supervisor = new SupervisorService();

        DefaultFileStorePersistenceAdapter filestore = new DefaultFileStorePersistenceAdapter(nobData);
        supervisor.setServerPersistenceApi(filestore);
        supervisor.setUpdatePersistenceApi(filestore);

        supervisor.init();

        Response answer = supervisor.createBroker();
        String id = (String) answer.getEntity();
        Assert.assertTrue(new File(nobData, id).exists());

        supervisor.deleteBroker(id);
        Assert.assertFalse(new File(nobData, id).exists());
    }

}
