package org.bgerp.itest.plugin.bil.billing.invoice;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "invoice", priority = 100, dependsOnGroups = { "config", "process" })
public class InvoiceTest {
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Plugin Invoice",
            PluginHelper.initPlugin(new org.bgerp.plugin.bil.billing.invoice.Plugin()) + ResourceHelper.getResource(this, "config.txt"));
    }
}