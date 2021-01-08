package org.bgerp.itest.plugin.feedback;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "feedback", priority = 100, dependsOnGroups = { "config", "openIface" })
public class FeedbackTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Feedback", 
            PluginHelper.initPlugin(new org.bgerp.plugin.msg.feedback.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}