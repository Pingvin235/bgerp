package org.bgerp.itest.plugin.svc.backup;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "backup", priority = 100, dependsOnGroups = "config")
public class BackupTest {
    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(new org.bgerp.plugin.svc.backup.Plugin(), ResourceHelper.getResource(this, "config.txt"));
    }
}