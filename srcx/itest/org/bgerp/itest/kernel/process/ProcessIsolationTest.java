package org.bgerp.itest.kernel.process;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.SimpleConfigMap;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.model.process.ProcessGroups;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;

@Test(groups = "processIsolation", dependsOnGroups = "process")
public class ProcessIsolationTest {
    private static final String TITLE = "Kernel Process Isolation";

    private int processTypeId;
    private int processTypeSpecialId;
    private int processType1Id;
    private int processType11Id;
    private int processType2Id;

    private int userGroupId;

    @Test
    public void userGroup() throws Exception {
        userGroupId = UserHelper.addGroup(TITLE, 0, UserHelper.GROUP_CONFIG_ISOLATION);
    }

    @Test(dependsOnMethods = "userGroup")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setAllowedGroups(new ProcessGroups(userGroupId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
        processTypeSpecialId = ProcessHelper.addType(TITLE + " special", processTypeId, false, null).getId();

        processType1Id = ProcessHelper.addType(TITLE + " 1", processTypeId, false, null).getId();
        processType11Id = ProcessHelper.addType(TITLE + " 1.1", processType1Id, false, props).getId();

        processType2Id  = ProcessHelper.addType(TITLE + " 2", processTypeId, false, null).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void testIsolationExecutor() throws Exception {
        String title = TITLE + " by Executor";

        var user = createUser(title, "isolated.executor");
        user.setConfig(SimpleConfigMap.of("isolation.process", "executor").getDataString());
        new UserDAO(DbTest.conRoot).updateUser(user);

        var dao = new ProcessDAO(DbTest.conRoot, new DynActionForm(user));

        var p = dao.updateProcess(new Process().withTypeId(processTypeId).withDescription(title));
        Assert.assertNull(dao.getProcess(p.getId()));

        dao.updateProcessExecutors(Set.of(new ProcessExecutor(user.getId(), userGroupId, 0)), p.getId());
        Assert.assertNotNull(dao.getProcess(p.getId()));
    }

    @Test(dependsOnMethods = "processType")
    public void testIsolationGroup() throws Exception {
        UserDAO udao = new UserDAO(DbTest.conRoot);

        String title = TITLE + " by Group";
        var user = createUser(title, "isolated.group");
        user.setConfig(SimpleConfigMap.of("isolation.process", "group").getDataString());
        udao.updateUser(user);

        var dao = new ProcessDAO(DbTest.conRoot, new DynActionForm(user));

        var p = dao.updateProcess(new Process().withTypeId(processTypeId).withDescription(title));
        Assert.assertNull(dao.getProcess(p.getId()));

        var ps = dao.updateProcess(new Process().withTypeId(processTypeSpecialId).withDescription(title + " Executor Types"));
        Assert.assertNull(dao.getProcess(ps.getId()));

        dao.updateProcessGroups(Set.of(new ProcessGroup(userGroupId)), p.getId());
        Assert.assertNotNull(dao.getProcess(p.getId()));
        Assert.assertNull(dao.getProcess(ps.getId()));

        title = title + " Executor Types";
        user = createUser(title, "isolated.group.executor.types");
        user.setConfig(SimpleConfigMap.of(
            "isolation.process", "group",
            "isolation.process.group.executor.typeIds", processTypeSpecialId + ", 0").getDataString());
        udao.updateUser(user);

        dao = new ProcessDAO(DbTest.conRoot, new DynActionForm(user));

        Assert.assertNotNull(dao.getProcess(p.getId()));
        Assert.assertNull(dao.getProcess(ps.getId()));

        dao.updateProcessGroups(Set.of(new ProcessGroup(userGroupId)), ps.getId());
        Assert.assertNull(dao.getProcess(ps.getId()));

        dao.updateProcessExecutors(Set.of(new ProcessExecutor(user.getId(), userGroupId, 0)), ps.getId());
        Assert.assertNotNull(dao.getProcess(ps.getId()));

        dao.updateProcessExecutors(Set.of(new ProcessExecutor(user.getId(), userGroupId, 0),
                new ProcessExecutor(-10, userGroupId, 5), new ProcessExecutor(22, 0, 5)), ps.getId());
        Assert.assertNotNull(dao.getProcess(ps.getId()));
    }

    @Test(dependsOnMethods = "processType")
    public void testCreateProcessTypes() throws Exception {
        String title = TITLE + " Create Process Types";

        var user = createUser(title, "isolated.create");
        user.setGroupIds(Set.of(userGroupId));
        user.setConfig(SimpleConfigMap.of("isolation.process", "group").getDataString());
        new UserDAO(DbTest.conRoot).updateUser(user);

        var typeList = ProcessTypeCache.getTypeList(
                Set.of(ProcessTest.processTypeTestGroupId, processTypeId, processTypeSpecialId, processType1Id, processType11Id, processType2Id));
        ProcessAction.applyProcessTypePermission(typeList, new DynActionForm(user));
        var ids = typeList.stream().map(ProcessType::getId).collect(Collectors.toSet());
        Assert.assertEquals(ids, Set.of(ProcessTest.processTypeTestGroupId, processTypeId, processType1Id, processType11Id));

        user.setConfig("");
        typeList = ProcessTypeCache.getTypeList(Set.of(processTypeId, processTypeSpecialId, processType2Id));
        ProcessAction.applyProcessTypePermission(typeList, new DynActionForm(user));
        ids = typeList.stream().map(ProcessType::getId).collect(Collectors.toSet());
        Assert.assertEquals(ids, Set.of(processTypeId, processTypeSpecialId, processType2Id));
    }

    private User createUser(String title, String login) throws Exception {
        var user = UserHelper.addUser(title, login, List.of(new UserGroup(userGroupId, new Date(), null)));
        UserHelper.addUserProcessQueues(user.getId(), Set.of(ProcessTest.queueId));
        return user;
    }
}
