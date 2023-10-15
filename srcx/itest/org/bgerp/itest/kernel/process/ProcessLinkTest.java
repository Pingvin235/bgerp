package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

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

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processLink", dependsOnGroups = { "processParam", "message" })
public class ProcessLinkTest {
    private static final String TITLE = "Kernel Process Link";
    private static final String TITLE_LINKED_AVAILABLE = TITLE + " Linked (Parent) Available";
    private static final String TITLE_LINK_AVAILABLE = TITLE + " Link (Child) Available";

    private int processTypeLinkedAvailableId;
    private int processTypeLinkAvailableId;
    private int processTypeId;

    private Customer customer;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));

        var processType = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
        processTypeId = processType.getId();

        processTypeLinkedAvailableId = ProcessHelper.addType(TITLE_LINKED_AVAILABLE, processTypeId, true, null).getId();
        processTypeLinkAvailableId = ProcessHelper.addType(TITLE_LINK_AVAILABLE, processTypeId, true, null).getId();

        props.setConfig(ConfigHelper.generateConstants(
            "LINKED_AVAILABLE_PROCESS_TYPE_ID", processTypeLinkedAvailableId,
            "LINK_AVAILABLE_PROCESS_TYPE_ID", processTypeLinkAvailableId,
            "LINKED_AVAILABLE_PROCESS_STATUS_ID", String.valueOf(ProcessTest.statusOpenId),
            "PROCESS_TYPE_ID", processTypeId
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

        int processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();
        MessageHelper.addHowToTestNoteMessage(processId, this);
        dao.addLink(new ProcessLink(processId, Customer.OBJECT_TYPE, customer.getId(), customer.getTitle()));

        // existing linked processes
        for (int i = 0; i <= 2; i++) {
            int parentProcessId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Linked (Parent) " + i).getId();
            dao.addLink(new ProcessLinkProcess.Depend(parentProcessId, processId));
            dao.addLink(new ProcessLinkProcess.Link(parentProcessId, processId));
        }

        for (int i = 0; i <= 2; i++) {
            int childProcessId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Link (Child) " + i).getId();
            dao.addLink(new ProcessLinkProcess.Made(processId, childProcessId));
            dao.addLink(new ProcessLinkProcess.Link(processId, childProcessId));
        }

        // available linked processes
        for (int i = 0; i <= 1; i++)
            ProcessHelper.addProcess(processTypeLinkedAvailableId, UserTest.USER_ADMIN_ID, TITLE_LINKED_AVAILABLE + " " + i).getId();

        for (int i = 0; i <= 1; i++)
            ProcessHelper.addProcess(processTypeLinkAvailableId, UserTest.USER_ADMIN_ID, TITLE_LINK_AVAILABLE + " " + i).getId();
    }
}
