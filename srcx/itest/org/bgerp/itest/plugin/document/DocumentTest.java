package org.bgerp.itest.plugin.document;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

import ru.bgcrm.plugin.document.Plugin;

@Test(groups = "document", priority = 100, dependsOnGroups = { "config" })
public class DocumentTest {
    private static final Plugin PLUGIN = new Plugin();

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN, ResourceHelper.getResource(this, "config.txt"));
    }
}