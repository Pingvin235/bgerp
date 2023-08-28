package org.bgerp.itest.plugin.msg.feedback;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.plugin.msg.feedback.Plugin;
import org.testng.annotations.Test;

@Test(groups = "feedback", priority = 100, dependsOnGroups = { "config", "openIface" })
public class FeedbackTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;

    @Test
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}