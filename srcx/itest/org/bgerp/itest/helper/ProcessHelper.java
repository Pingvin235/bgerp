package org.bgerp.itest.helper;

import java.util.Set;

import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.QueueDAO;
import ru.bgcrm.dao.process.StatusDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessLink;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;

public class ProcessHelper {

    public static int addStatus(String title, int pos) throws Exception {
        var con = DbTest.conRoot;

        var status = new Status(-1, title);
        status.setPos(pos);
        new StatusDAO(con).updateStatus(status);
        Assert.assertTrue(status.getId() > 0);

        return status.getId();
    }

    public static ProcessType addType(String title, int parentId, boolean useParentProperties, TypeProperties props) throws Exception {
        var con = DbTest.conRoot;
        var dao = new ProcessTypeDAO(con);

        var type = new ProcessType(-1, title);
        type.setParentId(parentId);
        type.setUseParentProperties(useParentProperties);
        dao.updateProcessType(type, User.USER_SYSTEM_ID);
        Assert.assertTrue(type.getId() > 0);

        if (!useParentProperties && props != null) {
            Preferences.processIncludes(new ConfigDAO(DbTest.conRoot), props.getConfig(), true);
            type.setProperties(props);
            dao.updateTypeProperties(type);
        }

        ProcessTypeCache.flush(DbTest.conRoot);

        return type;
    }

    public static int addQueue(String title, String config, Set<Integer> processTypeIds) throws Exception {
        var con = DbTest.conRoot;
        var dao = new QueueDAO(con);

        var queue = new Queue(-1, title);
        queue.setConfig(config);
        if (processTypeIds != null)
            queue.setProcessTypeIds(processTypeIds);
        dao.updateQueue(queue, User.USER_SYSTEM_ID);
        Assert.assertTrue(queue.getId() > 0);

        return queue.getId();
    }

    public static void addQueueTypes(int queueId, Set<Integer> processTypeIds) throws Exception {
        var con = DbTest.conRoot;
        var dao = new QueueDAO(con);

        var queue =  dao.getQueue(queueId);
        Assert.assertNotNull(queue);

        queue.getProcessTypeIds().addAll(processTypeIds);

        dao.updateQueue(queue, User.USER_SYSTEM_ID);
    }

    public static Process addProcess(int typeId, int createUserId, String description) throws Exception {
        var result = addProcess(typeId, createUserId, description, 0);
        Assert.assertTrue(0 < result.getId());
        return result;
    }

    // TODO: Creation time is always current.
    public static Process addProcess(int typeId, int createUserId, String description, int priority) throws Exception {
        var con = DbTest.conRoot;

        Process process = new Process();
        process.setTypeId(typeId);
        process.setDescription(description);
        process.setPriority(priority);

        ProcessAction.processCreate(new DynActionForm(UserCache.getUser(createUserId)), con, process);

        return process;
    }

    public static void addLink(ProcessLink link) throws Exception {
        new ProcessLinkDAO(DbTest.conRoot).addLink(link);
    }

    public static void addCustomerLink(int processId, String linkType, Customer customer) throws Exception {
        new ProcessLinkDAO(DbTest.conRoot).addLink(new ProcessLink(processId, linkType, customer.getId(), customer.getTitle()));
    }

    public static void addGroup(Process process, int groupId) throws Exception {
        addGroup(process, groupId, 0);
    }

    public static void addGroup(Process process, int groupId, int roleId) throws Exception {
        process.getGroups().add(new ProcessGroup(groupId, roleId));
        new ProcessDAO(DbTest.conRoot).updateProcessGroups(process.getGroups(), process.getId());
    }

    public static void addExecutor(Process process, int userId, int groupId) throws Exception {
        addExecutor(process, userId, groupId, 0);
    }

    public static void addExecutor(Process process, int userId, int groupId, int roleId) throws Exception {
        process.getExecutors().add(new ProcessExecutor(userId, groupId, roleId));
        new ProcessDAO(DbTest.conRoot).updateProcessExecutors(process.getExecutors(), process.getId());
    }
}