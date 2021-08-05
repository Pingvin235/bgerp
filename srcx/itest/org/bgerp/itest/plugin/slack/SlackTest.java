package org.bgerp.itest.plugin.slack;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "slack", priority = 100, dependsOnGroups = "config")
public class SlackTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Slack", 
            PluginHelper.initPlugin(new ru.bgcrm.plugin.slack.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}