package org.bgerp.itest.plugin.pln.callboard;

import static org.bgerp.itest.kernel.user.UserTest.userFriedrichId;
import static org.bgerp.itest.kernel.user.UserTest.userKarlId;
import static org.bgerp.itest.kernel.user.UserTest.userLeonId;
import static org.bgerp.itest.kernel.user.UserTest.userVladimirId;
import static org.bgerp.itest.kernel.user.UserTest.userVyacheslavId;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.AddressTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.pln.callboard.Plugin;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.work.ShiftDAO;
import ru.bgcrm.dao.work.WorkTypeDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.work.Shift;
import ru.bgcrm.model.work.WorkShift;
import ru.bgcrm.model.work.WorkType;
import ru.bgcrm.model.work.WorkTypeTime;
import ru.bgcrm.util.TimeUtils;

@Test(groups = "callboard", priority = 100, dependsOnGroups = { "config", "user", "process", "address" })
public class CallboardTest {
    private static final Plugin PLUGIN = new Plugin();
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int userGroupIdParent;
    private int userGroupIdSub1;
    private int userGroupIdSub2;

    /** From config.txt resource. */
    private static final int GRAPH_ID = 1;
    private static final int CATEGORY_COMMON_ID = 1;
    private static final int CATEGORY_CONNECTIONS_ID = 2;

    private int posParam = 100;

    private int paramAddressId;
    private int paramServicesId;
    private int paramConnectionTimeId;

    private int processTypeId;

    private int workTypeInetKtvId;
    private int workTypeInetId;
    private int workTypeLunchId;
    private int workTypeSickId;

    private int shiftInetKtvId;
    private int shiftInetId;

    @Test
    public void param() throws Exception {
        //ParamHelper.addParam(object, type, title, pos, config, valuesConfig)
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, TITLE + " Address", posParam += 1, "", "");
        paramServicesId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " Services", posParam += 1, ParamTest.MULTIPLE, "1=Интернет\n2=КТВ");
        paramConnectionTimeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " Connection Time", posParam += 1, "", "");
    }

    @Test
    public void userGroup() throws Exception {
        userGroupIdParent = UserHelper.addGroup(TITLE + " Group", 0);

        userGroupIdSub1 = UserHelper.addGroup(TITLE + " Sub1", userGroupIdParent);
        UserHelper.addUserGroups(userKarlId, userGroupIdSub1);
        UserHelper.addUserGroups(userLeonId, userGroupIdSub1);
        userGroupIdSub2 = UserHelper.addGroup(TITLE + " Sub2", userGroupIdParent);
        UserHelper.addUserGroups(userVladimirId, userGroupIdSub2);
        UserHelper.addUserGroups(userVyacheslavId, userGroupIdSub2);
        UserHelper.addUserGroups(userFriedrichId, userGroupIdSub2);
    }

    @Test(dependsOnMethods = { "param", "userGroup" })
    public void processType() throws Exception {
        var props = ProcessTypeCache.getProcessType(ProcessTest.processTypeTestId).getProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramAddressId, paramServicesId, paramConnectionTimeId));
        props.setConfig(
            ConfigHelper.generateConstants(
                "PROCESS_PARAM_CONNECTION_TIME_ID", paramConnectionTimeId,
                "PROCESS_PARAM_ADDRESS_ID", paramAddressId
            ) +
            ResourceHelper.getResource(this, "processType.config.txt"));
        processTypeId = ProcessHelper.addType(TITLE + " Connection", ProcessTest.processTypeTestGroupId, false, props);
    }

    @Test(dependsOnMethods = { "param", "processType" })
    public void process() throws Exception {
        var process = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Connection");
        ProcessHelper.addGroup(process, userGroupIdSub1);
        ProcessHelper.addExecutor(process, userKarlId, userGroupIdSub1);

        var dao = new ParamValueDAO(DbTest.conRoot);

        var addr = new ParameterAddressValue();
        addr.setHouseId(AddressTest.houseUfa6.getId());
        addr.setFlat("7");
        dao.updateParamAddress(process.getId(), paramAddressId, 0, addr);

        dao.updateParamList(process.getId(), paramServicesId, Set.of(1, 2));
    }

    @Test(dependsOnMethods = { "userGroup", "processType" })
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN,
            ConfigHelper.generateConstants("USER_GROUP_ID", userGroupIdParent) +
            ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "config")
    public void workType() throws Exception {
        var dao = new WorkTypeDAO(DbTest.conRoot);

        var workType = new WorkType();
        workType.setTitle("Подключение Интернет + КТВ");
        workType.setCategory(CATEGORY_CONNECTIONS_ID);
        workType.setColor("#008080");
        workType.setNonWorkHours(false);
        workType.setComment("Подключение Интернет + КТВ = 60 минут; \n" +
                            "Только Интернет = 30 минут");
        workType.setTimeSetStep(30);
        workType.setTimeSetMode(WorkType.MODE_TIME_ON_STEP);
        workType.setRuleConfig(
            ConfigHelper.generateConstants(
                "PROCESS_PARAM_SERVICES_ID", paramServicesId
            ) +
            ResourceHelper.getResource(this, "workType.1.config.txt"));
        dao.updateWorkType(workType);
        Assert.assertTrue(0 < (workTypeInetKtvId = workType.getId()));

        workType = new WorkType();
        workType.setTitle("Подключение Интернет");
        workType.setCategory(CATEGORY_CONNECTIONS_ID);
        workType.setColor("#008000");
        workType.setNonWorkHours(false);
        workType.setComment("Подключение Интернет = 30 минут");
        workType.setTimeSetStep(30);
        workType.setTimeSetMode(WorkType.MODE_TIME_ON_STEP);
        workType.setRuleConfig(ResourceHelper.getResource(this, "workType.2.config.txt"));
        dao.updateWorkType(workType);
        Assert.assertTrue(0 < (workTypeInetId = workType.getId()));

        workType = new WorkType();
        workType.setTitle("Обед");
        workType.setCategory(CATEGORY_COMMON_ID);
        workType.setColor("#ff6600");
        workType.setNonWorkHours(true);
        workType.setComment("");
        workType.setTimeSetStep(0);
        workType.setTimeSetMode(WorkType.MODE_TIME_ON_START);
        workType.setRuleConfig("");
        dao.updateWorkType(workType);
        Assert.assertTrue(0 < (workTypeLunchId = workType.getId()));

        workType = new WorkType();
        workType.setTitle("Болел");
        workType.setCategory(CATEGORY_COMMON_ID);
        workType.setColor("#ff0000");
        workType.setNonWorkHours(true);
        workType.setComment("");
        workType.setTimeSetStep(0);
        workType.setTimeSetMode(WorkType.MODE_TIME_ON_START);
        workType.setRuleConfig("");
        dao.updateWorkType(workType);
        Assert.assertTrue(0 < (workTypeSickId = workType.getId()));
    }

    @Test(dependsOnMethods = "workType")
    public void shift() throws Exception {
        var dao = new ShiftDAO(DbTest.conRoot);

        var shift = new Shift();
        shift.setCategory(CATEGORY_CONNECTIONS_ID);
        shift.setTitle("Подключения всего");
        shift.setUseOwnColor(true);
        shift.setSymbol("ИК");
        shift.setColor("#ff9900");

        var wtt = new WorkTypeTime();
        wtt.setWorkTypeId(workTypeInetKtvId);
        wtt.setDayMinuteFrom(540);
        wtt.setDayMinuteTo(720);
        shift.getWorkTypeTimeList().add(wtt);

        wtt = new WorkTypeTime();
        wtt.setWorkTypeId(workTypeLunchId);
        wtt.setDayMinuteFrom(720);
        wtt.setDayMinuteTo(780);
        shift.getWorkTypeTimeList().add(wtt);

        wtt = new WorkTypeTime();
        wtt.setWorkTypeId(workTypeInetKtvId);
        wtt.setDayMinuteFrom(780);
        wtt.setDayMinuteTo(1080);
        shift.getWorkTypeTimeList().add(wtt);

        dao.updateShift(shift);
        Assert.assertTrue(0 < (shiftInetKtvId = shift.getId()));

        shift = new Shift();
        shift.setCategory(CATEGORY_CONNECTIONS_ID);
        shift.setTitle("Подключения только Интернет");
        shift.setUseOwnColor(true);
        shift.setSymbol("И");
        shift.setColor("#008000");

        wtt = new WorkTypeTime();
        wtt.setWorkTypeId(workTypeInetId);
        wtt.setDayMinuteFrom(540);
        wtt.setDayMinuteTo(660);
        shift.getWorkTypeTimeList().add(wtt);

        wtt = new WorkTypeTime();
        wtt.setWorkTypeId(workTypeLunchId);
        wtt.setDayMinuteFrom(660);
        wtt.setDayMinuteTo(720);
        shift.getWorkTypeTimeList().add(wtt);

        wtt = new WorkTypeTime();
        wtt.setWorkTypeId(workTypeInetId);
        wtt.setDayMinuteFrom(720);
        wtt.setDayMinuteTo(1080);
        shift.getWorkTypeTimeList().add(wtt);

        dao.updateShift(shift);
        Assert.assertTrue(0 < (shiftInetId = shift.getId()));
    }

    @Test(dependsOnMethods = "workType")
    public void userShift() throws Exception {
        var dao = new ShiftDAO(DbTest.conRoot);

        var shift = dao.getShift(shiftInetKtvId);
        Assert.assertNotNull(shift);

        WorkShift workShift = new WorkShift();
        workShift.setGraphId(GRAPH_ID);
        workShift.setGroupId(userGroupIdSub1);
        workShift.setUserId(userKarlId);
        workShift.setDate(new Date());
        workShift.setWorkTypeTimeList(shift.getWorkTypeTimeList());
        workShift.setShiftId(shiftInetKtvId);

        dao.updateWorkShift(workShift);

        shift = dao.getShift(shiftInetId);
        Assert.assertNotNull(shift);

        workShift = new WorkShift();
        workShift.setGraphId(GRAPH_ID);
        workShift.setGroupId(userGroupIdSub1);
        workShift.setUserId(userKarlId);
        workShift.setDate(TimeUtils.getNextDay(new Date()));
        workShift.setWorkTypeTimeList(shift.getWorkTypeTimeList());
        workShift.setShiftId(shiftInetId);

        dao.updateWorkShift(workShift);
    }
}