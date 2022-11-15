package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.CustomerHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.ProcessLink;
import ru.bgcrm.model.process.ProcessLinkProcess;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processLink", dependsOnGroups = { "processParam", "message" })
public class ProcessLinkTest {
    private static final String TITLE = "Kernel Process Link";

    private int processTypeId;
    private Customer customer;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.getParameterIds().addAll(ProcessParamTest.paramIds);

        var processType = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
        processTypeId = processType.getId();

        props.setConfig(ConfigHelper.generateConstants("PROCESS_TYPE_ID", processTypeId) + ResourceHelper.getResource(this, "processType.txt"));
        new ProcessTypeDAO(DbTest.conRoot).updateTypeProperties(processType);

        ProcessTypeCache.flush(DbTest.conRoot);
    }

    @Test
    public void customer() throws Exception {
        customer = CustomerHelper.addCustomer(-1, -1, TITLE);
    }

    @Test(dependsOnMethods = { "processType", "customer" })
    public void process() throws Exception {
        var dao = new ProcessLinkDAO(DbTest.conRoot);

        int processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();
        ProcessParamTest.paramValues(processId);
        MessageHelper.addHowToTestNoteMessage(processId, this);
        dao.addLink(new ProcessLink(processId, Customer.OBJECT_TYPE, customer.getId(), customer.getTitle()));

        for (int i = 0; i <= 2; i++) {
            int parentProcessId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Parent " + i).getId();
            dao.addLink(new ProcessLinkProcess.Depend(parentProcessId, processId));
            dao.addLink(new ProcessLinkProcess.Link(parentProcessId, processId));
        }

        for (int i = 0; i <= 2; i++) {
            int childProcessId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Child " + i).getId();
            dao.addLink(new ProcessLinkProcess.Made(processId, childProcessId));
            dao.addLink(new ProcessLinkProcess.Link(processId, childProcessId));
        }
    }
}
