package org.bgerp.itest.plugin.dispatch;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.annotations.Test;

import ru.bgcrm.plugin.dispatch.Plugin;
import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.model.Dispatch;

@Test(groups = "dispatch", priority = 100, dependsOnGroups = { "config" })
public class DispatchTest {
    private static final Plugin PLUGIN = new Plugin();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }

    @Test
    public void dispatch() throws Exception {
        var dao = new DispatchDAO(DbTest.conRoot);

        Dispatch dispatch = new Dispatch();
        dispatch.setTitle("BGERP News");
        dao.dispatchUpdate(dispatch);

        dispatch = new Dispatch();
        dispatch.setTitle("BGERP Security Warnings");
        dao.dispatchUpdate(dispatch);
    }
}
