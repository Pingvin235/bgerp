package org.bgerp.itest.kernel.demo;

import org.bgerp.dao.DemoDAO;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.model.DemoEntity;
import org.testng.annotations.Test;

@Test(groups = "demo", dependsOnGroups = "dbInit")
public class DemoTest {
    @Test
    public void entity() throws Exception {
        var dao = new DemoDAO(DbTest.conRoot);

        String config = ResourceHelper.getResource(this, "config.txt");
        for (int i = 1; i < 33; i++) {
            var entity = new DemoEntity();
            entity.setTitle("Demo Entity " + i);
            entity.setConfig(config + i);
            dao.update(entity);
        }
    }
}
