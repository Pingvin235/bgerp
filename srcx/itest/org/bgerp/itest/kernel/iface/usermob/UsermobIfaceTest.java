package org.bgerp.itest.kernel.iface.usermob;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.AddressTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "usermobIface", dependsOnGroups = { "user", "process", "address" })
public class UsermobIfaceTest {
    private static final String TITLE = "Kernel Usermob Interface";

    private ProcessType processType;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setGroups(Set.of(new ProcessGroup(UserTest.groupAdminsId)));
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(ProcessTest.paramAddressId));
        props.setConfig(ResourceHelper.getResource(this, "processType.txt"));

        processType = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
            ConfigHelper.generateConstants(
                "PROCESS_PARAM_ADDRESS_ID", ProcessTest.paramAddressId) +
                ResourceHelper.getResource(this, "processQueue.txt"),
            Set.of(processType.getId()));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var statusDao = new StatusChangeDAO(DbTest.conRoot);
        var processDao = new ProcessDAO(DbTest.conRoot);
        var paramDao = new ParamValueDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processType.getId(), UserTest.USER_ADMIN_ID, TITLE + " 1");
        processDao.updateProcessExecutors(Set.of(new ProcessExecutor(UserTest.USER_ADMIN_ID, UserTest.groupAdminsId)), process.getId());
        statusDao.changeStatus(process, processType, new StatusChange(process.getId(), new Date(), UserTest.userFelixId, ProcessTest.statusProgressId, ""));
        paramDao.updateParamAddress(process.getId(), ProcessTest.paramAddressId, 0,
                new ParameterAddressValue().withHouseId(AddressTest.houseUfa6.getId()).withFlat("12"));

        /* process = ProcessHelper.addProcess(processType.getId(), 0, TITLE) */
    }
}
