package org.bgerp.itest.plugin.clb.calendar;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.clb.calendar.Plugin;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
@Test(groups = "calendar", priority = 100, dependsOnGroups = { "config", "openIface", "process" })
public class CalendarTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}