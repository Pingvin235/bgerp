/*package org.bgerp.itest.plugin.calendar;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.testng.annotations.Test;

@Test(groups = "calendar", priority = 100, dependsOnGroups = { "config", "openIface", "process" })
public class CalendarTest {
    private static int vacationRequestProcessTypeId;

    @Test
    public void addTypes() throws Exception {
        // TODO: Configuration.
        vacationRequestProcessTypeId = ProcessHelper.addType("Calendar: Vacation Request", ProcessTest.processTypeTestGroupId, false, null);
    }

    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Calendar",
            PluginHelper.initPlugin(new org.bgerp.plugin.calendar.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}*/