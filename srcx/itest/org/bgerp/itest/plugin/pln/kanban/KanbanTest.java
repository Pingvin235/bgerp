package org.bgerp.itest.plugin.pln.kanban;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.pln.kanban.Plugin;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "kanban", priority = 100, dependsOnGroups = { "process", "user", "message" })
public class KanbanTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int processTypeId;
    private int processQueueId;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusWaitId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        processQueueId = ProcessHelper.addQueue(TITLE, ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));

        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(processQueueId));
    }

    @Test(dependsOnMethods = "processQueue")
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, "");
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var open = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " New feature request", 8);
        MessageHelper.addHowToTestNoteMessage(open.getId(), this);

        ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Fix login bug", 4);
        ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Update dependencies", 0);

        var statusDao = new StatusChangeDAO(DbTest.conRoot);
        var type = ProcessTypeCache.getProcessType(processTypeId);

        var inProgress = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Refactor auth module", 4);
        statusDao.changeStatus(inProgress, type,
            new StatusChange(inProgress.getId(), new Date(), UserTest.USER_ADMIN_ID, ProcessTest.statusProgressId, ""));

        var waiting = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Review pull request", 0);
        statusDao.changeStatus(waiting, type,
            new StatusChange(waiting.getId(), new Date(), UserTest.USER_ADMIN_ID, ProcessTest.statusWaitId, ""));

        var done = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Setup CI pipeline", 0);
        statusDao.changeStatus(done, type,
            new StatusChange(done.getId(), new Date(), UserTest.USER_ADMIN_ID, ProcessTest.statusDoneId, ""));
    }
}
