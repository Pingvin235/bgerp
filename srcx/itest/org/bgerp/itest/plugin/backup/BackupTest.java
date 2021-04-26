package org.bgerp.itest.plugin.backup;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "backup", priority = 100, dependsOnGroups = "config")
public class BackupTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Backup", 
            PluginHelper.initPlugin(new org.bgerp.plugin.svc.backup.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}