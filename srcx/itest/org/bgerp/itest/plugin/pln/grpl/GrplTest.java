package org.bgerp.itest.plugin.pln.grpl;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.AddressTest;
import org.bgerp.itest.kernel.process.ProcessParamTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.model.param.Parameter;
import org.bgerp.plugin.pln.grpl.Plugin;
import org.bgerp.plugin.pln.grpl.dao.GrplDAO;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Test(groups = "grpl", priority = 100, dependsOnGroups = { "process", "processParam", "user", "message" })
public class GrplTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int userGroupFirstId;
    private int userGroupSecondId;
    private int userGroupThirdId;

    private int paramWorkTypeId;

    private int processTypeId;

    private int cityMuenchenId;
    private int cityUfaId;

    @Test
    public void userGroup() throws Exception {
        int groupId = UserHelper.addGroup(TITLE, 0, "");
        userGroupFirstId = UserHelper.addGroup(TITLE + " Group 1", groupId, "");
        userGroupSecondId = UserHelper.addGroup(TITLE + " Group 2", groupId, "");
        userGroupThirdId = UserHelper.addGroup(TITLE + " Group 3", groupId, "");
        UserCache.flush(null);
    }

    @Test
    public void param() throws Exception {
        paramWorkTypeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " Work Type", ProcessTest.posParam += 2,
                "#multiple=1", ResourceHelper.getResource(this, "work.types.txt"));
    }

    @Test(dependsOnMethods = {"userGroup", "param"})
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId, ProcessTest.statusRejectId));
        props.setParameterIds(List.of(ProcessParamTest.paramAddressId, paramWorkTypeId));
        props.setConfig(ConfigHelper.generateConstants(
            "PARAM_ADDRESS_ID", ProcessParamTest.paramAddressId,
            "PARAM_WORK_TYPE_ID", paramWorkTypeId
        ) + ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "BOARD_TITLE", PLUGIN.getTitle() + " Board",
                "PROCESS_TYPE_IDS", processTypeId,
                "CITY_MUENCHEN_ID", cityMuenchenId = AddressTest.cityMuenchen.getId(),
                "CITY_UFA_ID", cityUfaId = AddressTest.cityUfa.getId(),
                "CITY_STERLITAMAK_ID", AddressTest.citySterlitamak.getId(),
                "PARAM_ADDRESS_ID", ProcessParamTest.paramAddressId,
                "USER_GROUP_IDS", Utils.toString(Set.of(userGroupFirstId, userGroupSecondId, userGroupThirdId)),
                "PARAM_WORK_TYPE_ID", paramWorkTypeId,
                "STATUS_DONE_ID", ProcessTest.statusDoneId,
                "STATUS_REJECT_ID", ProcessTest.statusRejectId
            ) + ResourceHelper.getResource(this, "config.txt")
        );
    }

    @Test(dependsOnMethods = "config")
    public void board() throws Exception {
        var config = PLUGIN.getConfig(Setup.getSetup());
        var board = config.getBoardOrThrow(1);

        var dao = new GrplDAO(DbTest.conRoot);
        var paramDao = new ParamValueDAO(DbTest.conRoot);
        var processDao = new ProcessDAO(DbTest.conRoot);
        var conSet = new SingleConnectionSet(DbTest.conRoot);

        var yesterday = TimeUtils.getPrevDay(new Date());
        dao.updateGroup(board.getId(), yesterday, cityMuenchenId, userGroupThirdId);
        dao.updateGroup(board.getId(), yesterday, cityUfaId, userGroupFirstId);

        for (int i = 1; i <= 3; i++) {
            var process = ProcessHelper.addProcess(processTypeId, TITLE + " München Yesterday " + i);
            paramDao.updateParamAddress(process.getId(), ProcessParamTest.paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseMuenchen.getId()));
            paramDao.updateParamList(process.getId(), paramWorkTypeId, Set.of(i));
            EventProcessor.processEvent(new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramWorkTypeId), process.getId(), null), conSet);

            var duration = board.getProcessDuration(conSet, process);
            if (i == 1) {
                processDao.updateProcess(process.withStatusId(ProcessTest.statusDoneId));
                dao.updateSlotTime(board.getId(), process.getId(), yesterday, board.getShift().getFrom().plus(duration));
            } else if (i == 2) {
                processDao.updateProcess(process.withStatusId(ProcessTest.statusRejectId));
                dao.updateSlotTime(board.getId(), process.getId(), yesterday, board.getShift().getFrom().plus(duration.multipliedBy(3)));
            } else
                dao.updateSlotTime(board.getId(), process.getId(), yesterday, LocalTime.of(14, 00));
        }

        var today = new Date();
        dao.updateGroup(board.getId(), today, cityMuenchenId, userGroupFirstId);
        dao.updateGroup(board.getId(), today, cityUfaId, userGroupSecondId);

        for (int i = 1; i <= 3; i++) {
            var process = ProcessHelper.addProcess(processTypeId, TITLE + " München Today " + i);
            paramDao.updateParamAddress(process.getId(), ProcessParamTest.paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseMuenchen.getId()));
            paramDao.updateParamList(process.getId(), paramWorkTypeId, Set.of(i));
            EventProcessor.processEvent(new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramWorkTypeId), process.getId(), null), conSet);

            var duration = board.getProcessDuration(conSet, process);
            if (i == 1) {
                processDao.updateProcess(process.withStatusId(ProcessTest.statusRejectId));
                dao.updateSlotTime(board.getId(), process.getId(), today, board.getShift().getFrom().plus(duration));
            } else if (i == 2) {
                processDao.updateProcess(process.withStatusId(ProcessTest.statusDoneId));
                dao.updateSlotTime(board.getId(), process.getId(), today, board.getShift().getFrom().plus(duration.multipliedBy(3)));
            } else
                dao.updateSlotTime(board.getId(), process.getId(), today, LocalTime.of(15, 00));
        }

        var tomorrow = TimeUtils.getNextDay(today);
        dao.updateGroup(board.getId(), tomorrow, cityMuenchenId, userGroupSecondId);
        dao.updateGroup(board.getId(), tomorrow, cityUfaId, userGroupThirdId);

        for (int i = 1; i <= 3; i++) {
            var process = ProcessHelper.addProcess(processTypeId, TITLE + " München Tomorrow " + i);
            paramDao.updateParamAddress(process.getId(), ProcessParamTest.paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseMuenchen.getId()));
            paramDao.updateParamList(process.getId(), paramWorkTypeId, Set.of(i));
            EventProcessor.processEvent(new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramWorkTypeId), process.getId(), null), conSet);

            var duration = board.getProcessDuration(conSet, process);
            if (i == 1)
                dao.updateSlotTime(board.getId(), process.getId(), tomorrow, board.getShift().getFrom().plus(duration));
            else if (i == 2)
                dao.updateSlotTime(board.getId(), process.getId(), tomorrow, board.getShift().getFrom().plus(duration.multipliedBy(3)));
            else
                dao.updateSlotTime(board.getId(), process.getId(), tomorrow, null);
        }

        for (int i = 1; i <= 3; i++) {
            var process = ProcessHelper.addProcess(processTypeId, TITLE + " Ufa " + i);
            paramDao.updateParamAddress(process.getId(), ProcessParamTest.paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseUfa6.getId()));
            int work = (3 + i) % 5 + 1;
            paramDao.updateParamList(process.getId(), paramWorkTypeId, Set.of(work));
            EventProcessor.processEvent(new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramWorkTypeId), process.getId(), null), conSet);

            var duration = board.getProcessDuration(conSet, process);
            if (work == 5)
                Assert.assertEquals(duration, Duration.ofMinutes(60));
            else
                Assert.assertEquals(duration, Duration.ofMinutes(30));
        }
    }
}