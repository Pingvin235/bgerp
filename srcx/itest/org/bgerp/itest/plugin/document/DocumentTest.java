package org.bgerp.itest.plugin.document;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.CustomerHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessParamTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.ProcessLink;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.plugin.document.Plugin;

@Test(groups = "document", priority = 100, dependsOnGroups = { "config", "customer", "process", "processParam" })
public class DocumentTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private Customer customer;
    private int processTypeId;

    @Test
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "PARAM_PROCESS_ADDRESS_ID", ProcessParamTest.paramAddressId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test
    public void customer() throws Exception {
        customer = CustomerHelper.addCustomer(-1, 0, TITLE);

        // TODO: Customer documents.
    }

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "processType.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = { "customer", "processType" })
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();

        new ProcessLinkDAO(DbTest.conRoot).addLink(new ProcessLink(processId, Customer.OBJECT_TYPE, customer.getId(), TITLE));

        // TODO: Process documents.
    }
}
