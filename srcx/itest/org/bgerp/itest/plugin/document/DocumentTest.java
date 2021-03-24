package org.bgerp.itest.plugin.document;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "document", priority = 100, dependsOnGroups = { "config" })
public class DocumentTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Document", 
            PluginHelper.initPlugin(new ru.bgcrm.plugin.document.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}