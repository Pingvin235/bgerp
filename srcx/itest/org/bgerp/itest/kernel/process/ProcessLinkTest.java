package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processLink", dependsOnGroups = { "processParam" })
public class ProcessLinkTest {
    private static final String TITLE = "Kernel Process Link";

    private int processTypeId;
    private int processId;

    @Test
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.getParameterIds().addAll(ProcessParamTest.paramIds);

        var processType = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
        processTypeId = processType.getId();

        props.setConfig(ConfigHelper.generateConstants("PROCESS_TYPE_ID", processTypeId) + ResourceHelper.getResource(this, "processType.txt"));
        new ProcessTypeDAO(DbTest.conRoot).updateTypeProperties(processType);

        ProcessTypeCache.flush(DbTest.conRoot);
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        processId = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE).getId();
        ProcessParamTest.paramValues(processId);
    }
}
