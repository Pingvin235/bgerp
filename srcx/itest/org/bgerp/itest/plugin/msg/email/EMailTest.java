package org.bgerp.itest.plugin.msg.email;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.msg.email.Plugin;
import org.testng.annotations.Test;

@Test(groups = "email", priority = 100, dependsOnGroups = { "config", "openIface" })
public class EMailTest {
    private static final Plugin PLUGIN = new Plugin();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}