package org.bgerp.itest.plugin.slack;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

import ru.bgcrm.plugin.slack.Plugin;

@Test(groups = "slack", priority = 100, dependsOnGroups = "config")
public class SlackTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;

    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}