package org.bgerp.itest.kernel.iface.usermob;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.AddressTest;
import org.bgerp.itest.kernel.process.ProcessParamTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.process.ProcessGroups;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.StatusChangeDAO;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "usermobIface", dependsOnGroups = { "user", "processParam", "address" })
public class UsermobIfaceTest {
    private static final String TITLE = "Kernel Usermob Interface Process Wizard";

    private ProcessType processType;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setGroups(new ProcessGroups(UserTest.groupAdminsId));
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(ProcessParamTest.paramAddressId));
        props.setConfig(ResourceHelper.getResource(this, "process.type.config.txt"));

        processType = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE, ConfigHelper.generateConstants(
                "PROCESS_PARAM_ADDRESS_ID", ProcessParamTest.paramAddressId,
                "PROCESS_TYPE_ID", processType.getId()
            ) + ResourceHelper.getResource(this, "process.queue.config.txt"),
            Set.of(processType.getId()));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var statusDao = new StatusChangeDAO(DbTest.conRoot);
        var paramDao = new ParamValueDAO(DbTest.conRoot);

        for (int i = 0; i <= 5; i++) {
            var process = ProcessHelper.addProcess(processType.getId(), TITLE + " " + i);
            if (i % 2 == 0)
                statusDao.changeStatus(process, processType,
                        new StatusChange(process.getId(), new Date(), UserTest.userFelixId, ProcessTest.statusProgressId, ""));
            paramDao.updateParamAddress(process.getId(), ProcessParamTest.paramAddressId, 0,
                    new ParameterAddressValue().withHouseId(AddressTest.houseUfa6.getId()).withFlat("1" + i));
        }
    }
}
