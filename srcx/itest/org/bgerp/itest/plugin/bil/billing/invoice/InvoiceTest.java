package org.bgerp.itest.plugin.bil.billing.invoice;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.PluginHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.bil.billing.invoice.Config;
import org.bgerp.plugin.bil.billing.invoice.dao.InvoiceDAO;
import org.bgerp.plugin.bil.billing.invoice.model.InvoiceType;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

@Test(groups = "invoice", priority = 100, dependsOnGroups = { "config", "process" })
public class InvoiceTest {
    private static final String TITLE = "Plugin Invoice";

    private Process process;
    private InvoiceType type;

    @Test
    public void process() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "processType.txt"));

        var processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);

        process = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);
    }

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(TITLE,
            PluginHelper.initPlugin(new org.bgerp.plugin.bil.billing.invoice.Plugin()) + ResourceHelper.getResource(this, "config.txt"));

        Assert.assertNotNull(type = Setup.getSetup().getConfig(Config.class).getType(1));
    }

    @Test(dependsOnMethods = { "process", "config" })
    public void invoice() throws Exception {
        var invoice = type.invoice(process.getId(), YearMonth.now().minusMonths(1));
        Assert.assertNotNull(invoice);
        Assert.assertEquals(1, invoice.getPositions().size());
        var pos = invoice.getPositions().get(0);
        Assert.assertEquals("test", pos.getId());
        Assert.assertEquals("Test position", pos.getTitle());
        Assert.assertEquals(Utils.parseBigDecimal("42.50"), pos.getAmount());

        new InvoiceDAO(DbTest.conRoot).update(invoice);
        Assert.assertTrue(invoice.getId() > 0);
    }
}