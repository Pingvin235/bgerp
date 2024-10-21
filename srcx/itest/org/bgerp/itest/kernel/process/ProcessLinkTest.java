package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.CustomerHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.model.process.link.ProcessLinkProcess;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processLink", dependsOnGroups = { "processParam", "message" })
public class ProcessLinkTest {
    private static final String TITLE = "Kernel Process Link";
    private static final String TITLE_LINKED_MADE = TITLE + " Process Linked (Parent) Made";
    private static final String TITLE_LINK_DEPEND = TITLE + " Process Link (Child) Depend";

    private int processTypeId;
    private int processTypeLinkedMadeId;
    private int processTypeLinkDependId;

    private Customer customer;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));

        var processType = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
        processTypeId = processType.getId();

        var processTypeLinkedMade = ProcessHelper.addType(TITLE_LINKED_MADE, processTypeId, false, props);
        processTypeLinkedMadeId = processTypeLinkedMade.getId();
        props.setConfig(ConfigHelper.generateConstants(
            "PROCESS_TYPE_ID", processTypeId
        ) + ResourceHelper.getResource(this, "process.linked.type.config.txt"));
        new ProcessTypeDAO(DbTest.conRoot).updateTypeProperties(processTypeLinkedMade);

        var processTypeLinkDepend = ProcessHelper.addType(TITLE_LINK_DEPEND, processTypeId, false, props);
        processTypeLinkDependId = processTypeLinkDepend.getId();
        props.setConfig(ConfigHelper.generateConstants(
            "PROCESS_TYPE_ID", processTypeId
        ) + ResourceHelper.getResource(this, "process.link.type.config.txt"));
        new ProcessTypeDAO(DbTest.conRoot).updateTypeProperties(processTypeLinkDepend);

        props.setConfig(ConfigHelper.generateConstants(
            "LINKED_MADE_PROCESS_TYPE_ID", processTypeLinkedMadeId,
            "LINK_DEPEND_PROCESS_TYPE_ID", processTypeLinkDependId,
            "LINK_DEPEND_PROCESS_STATUS_ID", String.valueOf(ProcessTest.statusOpenId)
        ) + ResourceHelper.getResource(this, "process.type.config.txt"));
        new ProcessTypeDAO(DbTest.conRoot).updateTypeProperties(processType);

        ProcessTypeCache.flush(DbTest.conRoot);
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE, ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test
    public void customer() throws Exception {
        customer = CustomerHelper.addCustomer(-1, -1, TITLE);
    }

    @Test(dependsOnMethods = { "processType", "customer" })
    public void process() throws Exception {
        var dao = new ProcessLinkDAO(DbTest.conRoot);

        int processId = ProcessHelper.addProcess(processTypeId, TITLE).getId();
        MessageHelper.addHowToTestNoteMessage(processId, this);
        dao.addLink(new ProcessLink(processId, Customer.OBJECT_TYPE, customer.getId(), customer.getTitle()));

        // existing linked processes
        for (int i = 0; i <= 2; i++) {
            int parentProcessId = ProcessHelper.addProcess(processTypeLinkedMadeId, TITLE_LINKED_MADE + " " + i).getId();
            dao.addLink(new ProcessLinkProcess.Made(parentProcessId, processId));
        }

        for (int i = 0; i <= 2; i++) {
            int childProcessId = ProcessHelper.addProcess(processTypeLinkDependId, TITLE_LINK_DEPEND + " " + i).getId();
            dao.addLink(new ProcessLinkProcess.Depend(processId, childProcessId));
        }

        // available to be linked processes
        for (int i = 0; i <= 1; i++)
            ProcessHelper.addProcess(processTypeLinkedMadeId, TITLE_LINKED_MADE + " a" + i).getId();

        for (int i = 0; i <= 1; i++)
            ProcessHelper.addProcess(processTypeLinkDependId, TITLE_LINK_DEPEND + " a" + i).getId();
    }
}
