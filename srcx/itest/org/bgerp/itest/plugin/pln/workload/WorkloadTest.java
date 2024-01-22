package org.bgerp.itest.plugin.pln.workload;

import static org.bgerp.itest.kernel.user.UserTest.userVladimirId;
import static org.bgerp.itest.kernel.user.UserTest.userVyacheslavId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.AddressTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.pln.workload.Plugin;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "workload", priority = 100, dependsOnGroups = { "config", "user", "process", "address" })
public class WorkloadTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int userGroupId;

    private int paramAddressId;
    private int paramDateTimeFromId;
    private int paramDateTimeToId;

    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, TITLE + " Address", ProcessTest.posParam += 2, "", "");
        paramDateTimeFromId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " DateTime From", ProcessTest.posParam += 2, "type=ymdhm", "");
        paramDateTimeToId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " DateTime To", ProcessTest.posParam += 2, "type=ymdhm", "");
    }

    @Test
    public void userGroup() throws Exception {
        userGroupId = UserHelper.addGroup(TITLE + " Group", 0, "");
        UserHelper.addUserGroups(userVladimirId, userGroupId);
        UserHelper.addUserGroups(userVyacheslavId, userGroupId);
    }

    @Test(dependsOnMethods = { "param", "userGroup" })
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramAddressId, paramDateTimeFromId, paramDateTimeToId));

        var type = ProcessHelper.addType(TITLE + " Work", ProcessTest.processTypeTestGroupId, false, props);
        processTypeId = type.getId();

        props.setConfig(
            ConfigHelper.generateConstants(
                "USER_GROUP_IDS", userGroupId,
                "PROCESS_TYPE_IDS", processTypeId,
                "PROCESS_PARAM_DATE_FROM_ID", paramDateTimeFromId,
                "PROCESS_PARAM_DATE_TO_ID", paramDateTimeToId,
                "PROCESS_PARAM_ADDRESS_ID", paramAddressId
            ) + ResourceHelper.getResource(this, "process.type.config.txt"));

        new ProcessTypeDAO(DbTest.conRoot).updateTypeProperties(type);
    }

    @Test(dependsOnMethods = { "param", "processType" })
    public void process() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot);

        LocalDate today = LocalDate.now();

        var process = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Work 1");
        ProcessHelper.addGroup(process, userGroupId);
        ProcessHelper.addExecutor(process, userVyacheslavId, userGroupId);

        var addr = new ParameterAddressValue();
        addr.setHouseId(AddressTest.houseUfa6.getId());
        addr.setFlat("71");
        dao.updateParamAddress(process.getId(), paramAddressId, 0, addr);
        dao.updateParamDateTime(process.getId(), paramDateTimeFromId, Date.from(LocalDateTime.of(today, LocalTime.of(12, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        dao.updateParamDateTime(process.getId(), paramDateTimeToId, Date.from(LocalDateTime.of(today, LocalTime.of(18, 20)).atZone(ZoneId.systemDefault()).toInstant()));

        process = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Work 2");
        ProcessHelper.addGroup(process, userGroupId);
        ProcessHelper.addExecutor(process, userVladimirId, userGroupId);

        addr = new ParameterAddressValue();
        addr.setHouseId(AddressTest.houseUfa6.getId());
        addr.setFlat("72");
        dao.updateParamAddress(process.getId(), paramAddressId, 0, addr);
        dao.updateParamDateTime(process.getId(), paramDateTimeFromId, Date.from(LocalDateTime.of(today, LocalTime.of(14, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        dao.updateParamDateTime(process.getId(), paramDateTimeToId, Date.from(LocalDateTime.of(today, LocalTime.of(17, 10)).atZone(ZoneId.systemDefault()).toInstant()));
    }

    @Test(dependsOnMethods = { "userGroup", "processType" })
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN, "");
    }
}