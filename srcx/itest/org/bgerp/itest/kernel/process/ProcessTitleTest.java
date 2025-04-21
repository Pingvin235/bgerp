package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.model.param.Parameter;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Test(groups = "processTitle", dependsOnGroups = { "process", "processParam", "message" })
public class ProcessTitleTest {
    private static final String TITLE = "Kernel Process Title";
    private static final String DESCRIPTION =  TITLE + " with special chars: <> \" '";

    private int paramListId;
    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramListId = ParamHelper.addParam(ru.bgcrm.model.process.Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " param 'list'",
                ProcessTest.posParam += 2, "", ParamTest.LIST_VALUES_123);
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramListId));
        props.setConfig(ConfigHelper.generateConstants("PARAM_LIST_ID", paramListId)
                + ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, DESCRIPTION).getId();

        var processDao = new ProcessDAO(DbTest.conRoot);
        var paramDao = new ParamValueDAO(DbTest.conRoot);

        var process = processDao.getProcess(processId);
        Assert.assertEquals(process.getTitle(), "LIST VALUE: ; DESCRIPTION: " + DESCRIPTION);

        Set<Integer> value = Set.of(2);
        paramDao.updateParamList(processId, paramListId, value);

        var form = new DynActionForm(User.USER_SYSTEM);
        EventProcessor.processEvent(new ParamChangedEvent(form, ParameterCache.getParameter(paramListId), processId, value),
                new SingleConnectionSet(DbTest.conRoot));
        Assert.assertEquals(form.getResponse().getEventList().size(), 1);

        process = processDao.getProcess(processId);
        Assert.assertEquals(process.getTitle(), "LIST VALUE: Value2; DESCRIPTION: " + DESCRIPTION);

        MessageHelper.addHowToTestNoteMessage(processId, this);
    }
}
