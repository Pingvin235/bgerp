package org.bgerp.itest.kernel.process;

import java.util.Date;
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
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Test(groups = "processDescription", dependsOnGroups = { "process", "processParam", "message" })
public class ProcessDescriptionTest {
    private static final String TITLE = "Kernel Process Description";

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
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusProgressId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramListId));
        props.setConfig(ConfigHelper.generateConstants(
            "TITLE", TITLE,
            "PARAM_LIST_ID", paramListId
            ) + ResourceHelper.getResource(this, "process.type.config.txt"));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, TITLE).getId();

        var processDao = new ProcessDAO(DbTest.conRoot);

        var process = processDao.getProcess(processId);
        Assert.assertEquals(process.getDescription(), TITLE + " LIST VALUE: ; STATUS: Open");

        Set<Integer> value = Set.of(2);
        new ParamValueDAO(DbTest.conRoot).updateParamList(processId, paramListId, value);

        var form = new DynActionForm(User.USER_SYSTEM);

        EventProcessor.processEvent(new ParamChangedEvent(form, ParameterCache.getParameter(paramListId), processId, value), new SingleConnectionSet(DbTest.conRoot));
        Assert.assertEquals(form.getResponse().getEventList().size(), 1);

        process = processDao.getProcess(processId);
        Assert.assertEquals(process.getDescription(), TITLE + " LIST VALUE: Value2; STATUS: Open");

        ProcessAction.processStatusUpdate(form, DbTest.conRoot, process, new StatusChange(processId, new Date(), User.USER_SYSTEM_ID, ProcessTest.statusProgressId, ""));

        process = processDao.getProcess(processId);
        Assert.assertEquals(process.getDescription(), TITLE + " LIST VALUE: Value2; STATUS: Progress");

        MessageHelper.addHowToTestNoteMessage(processId, this);
    }
}
