package org.bgerp.itest.plugin.clb.team;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.clb.team.Plugin;
import org.testng.annotations.Test;

@Test(groups = "team", priority = 100, dependsOnGroups = { "config", "openIface" })
public class TeamTest {
    private static final Plugin PLUGIN = new Plugin();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}
