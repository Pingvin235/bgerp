package org.bgerp.itest.kernel.process;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;

@Test(groups = "processIsolation", dependsOnGroups = "process")
public class ProcessIsolationTest {
    private static final String TITLE = "Kernel Process Isolation";

    private int processTypeId;
    private int processTypeSpecialId;
    private int processType1Id;
    private int processType11Id;
    private int processType2Id;

    private int userGroupId;
    private User userIsolated;

    @Test
    public void userGroup() throws Exception {
        userGroupId = UserHelper.addGroup(TITLE, 0, UserHelper.GROUP_CONFIG_ISOLATION);
    }

    @Test(dependsOnMethods = "userGroup")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setAllowedGroups(Set.of(new ProcessGroup(userGroupId)));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
        processTypeSpecialId = ProcessHelper.addType(TITLE + " special", processTypeId, false, null);

        processType1Id = ProcessHelper.addType(TITLE + " 1", processTypeId, false, null);
        processType11Id = ProcessHelper.addType(TITLE + " 1.1", processType1Id, false, props);

        processType2Id  = ProcessHelper.addType(TITLE + " 2", processTypeId, false, null);

        ProcessTypeCache.flush(null);
    }

    @Test(dependsOnMethods = "processType")
    public void user() throws Exception {
        userIsolated = UserHelper.addUser(TITLE, "isolated", List.of(new UserGroup(userGroupId, new Date(), null)));
        UserHelper.addUserProcessQueues(userIsolated.getId(), Set.of(ProcessTest.queueId));
    }

    @Test(dependsOnMethods = "user")
    public void testIsolationExecutor() throws Exception {
        userIsolated.setConfig(ParameterMap.of("isolation.process", "executor").getDataString());

        var dao = new ProcessDAO(DbTest.conRoot, userIsolated);

        var p = dao.updateProcess(new Process().withTypeId(processTypeId).withDescription(TITLE + " by executor"));
        Assert.assertNull(dao.getProcess(p.getId()));

        dao.updateProcessExecutors(Set.of(new ProcessExecutor(userIsolated.getId(), userGroupId, 0)), p.getId());
        Assert.assertNotNull(dao.getProcess(p.getId()));
    }

    @Test(dependsOnMethods = "user")
    public void testIsolationGroups() throws Exception {
        userIsolated.setConfig(ParameterMap.of("isolation.process", "group").getDataString());

        var dao = new ProcessDAO(DbTest.conRoot, userIsolated);

        var p = dao.updateProcess(new Process().withTypeId(processTypeId).withDescription(TITLE + " by group"));
        Assert.assertNull(dao.getProcess(p.getId()));

        var ps = dao.updateProcess(new Process().withTypeId(processTypeSpecialId).withDescription(TITLE + " by group special type"));
        Assert.assertNull(dao.getProcess(ps.getId()));

        dao.updateProcessGroups(Set.of(new ProcessGroup(userGroupId)), p.getId());
        Assert.assertNotNull(dao.getProcess(p.getId()));
        Assert.assertNull(dao.getProcess(ps.getId()));

        userIsolated.setConfig(ParameterMap.of("isolation.process", "group", "isolation.process.group.executor.typeIds",
                processTypeSpecialId + ", 0").getDataString());
        Assert.assertNotNull(dao.getProcess(p.getId()));
        Assert.assertNull(dao.getProcess(ps.getId()));

        dao.updateProcessGroups(Set.of(new ProcessGroup(userGroupId)), ps.getId());
        Assert.assertNull(dao.getProcess(ps.getId()));

        dao.updateProcessExecutors(Set.of(new ProcessExecutor(userIsolated.getId(), userGroupId, 0)), ps.getId());
        Assert.assertNotNull(dao.getProcess(ps.getId()));

        dao.updateProcessExecutors(Set.of(new ProcessExecutor(userIsolated.getId(), userGroupId, 0),
                new ProcessExecutor(-10, userGroupId, 5), new ProcessExecutor(22, 0, 5)), ps.getId());
        Assert.assertNotNull(dao.getProcess(ps.getId()));
    }

    @Test(dependsOnMethods = { "processType", "user" })
    public void testCreateProcessTypes() throws Exception {
        userIsolated.setGroupIds(Set.of(userGroupId));

        userIsolated.setConfig(ParameterMap.of("isolation.process", "group").getDataString());
        var typeList = ProcessTypeCache.getTypeList(
                Set.of(ProcessTest.processTypeTestGroupId, processTypeId, processTypeSpecialId, processType1Id, processType11Id, processType2Id));
        ProcessAction.applyProcessTypePermission(typeList, new DynActionForm(userIsolated));
        var ids = typeList.stream().map(ProcessType::getId).collect(Collectors.toSet());
        Assert.assertEquals(ids, Set.of(ProcessTest.processTypeTestGroupId, processTypeId, processType1Id, processType11Id));

        userIsolated.setConfig("");
        typeList = ProcessTypeCache.getTypeList(Set.of(processTypeId, processTypeSpecialId, processType2Id));
        ProcessAction.applyProcessTypePermission(typeList, new DynActionForm(userIsolated));
        ids = typeList.stream().map(ProcessType::getId).collect(Collectors.toSet());
        Assert.assertEquals(ids, Set.of(processTypeId, processTypeSpecialId, processType2Id));
    }
}
