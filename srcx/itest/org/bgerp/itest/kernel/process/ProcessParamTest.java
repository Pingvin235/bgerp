package org.bgerp.itest.kernel.process;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

@Test(groups = "processParam", dependsOnGroups = { "process", "param", "address" })
public class ProcessParamTest {
    private static final String TITLE = "Kernel Process Param";

    private int paramAddressId;
    private int paramDateId;
    private int paramDatetimeId;
    private int paramEmailId;
    private int paramFileId;
    private int paramListId;
    private int paramListCountId;
    private int paramMoneyId;
    private int paramTextId;
    private int paramTextRegexpId;
    private int paramTreeId;
    private int processTypeId;
    private int processId;

    @Test
    public void addParam() throws Exception {
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, TITLE + " type 'address'",
                ProcessTest.posParam += 2, "", "");

        paramDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " type 'date'",
                ProcessTest.posParam += 2, "", "");

        paramDatetimeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " type 'datetime'",
                ProcessTest.posParam += 2, "", "");

        paramEmailId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_EMAIL, TITLE + " type 'email'",
                ProcessTest.posParam += 2, "", "");

        paramFileId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_FILE, TITLE + " type 'file'",
                ProcessTest.posParam += 2, "", "");

        paramListId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST,
                TITLE + " type 'list'", ProcessTest.posParam += 2,
                ResourceHelper.getResource(this, "param.list.config.txt"),
                ResourceHelper.getResource(this, "param.list.values.txt"));

        // TODO: list with directory (users)

        paramListCountId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LISTCOUNT,
                TITLE + " type 'listcount'", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "param.list.values.txt"));

        paramMoneyId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " type 'money'",
                ProcessTest.posParam += 2, "", "");

        paramTextId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " type 'text'",
                ProcessTest.posParam += 2, ResourceHelper.getResource(this, "param.text.config.txt"), "");

        paramTextRegexpId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " type 'text' regexp",
                ProcessTest.posParam += 2, ResourceHelper.getResource(this, "param.text.regexp.config.txt"), "");

        paramTreeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TREE, TITLE + " type 'tree'",
                ProcessTest.posParam += 2, "", ResourceHelper.getResource(this, "param.tree.values.txt"));

        ParameterCache.flush(null);
    }

    @Test(dependsOnMethods = "addParam")
    public void addProcessType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.getParameterIds().addAll(List.of(paramAddressId, paramDateId, paramDatetimeId, paramEmailId, paramFileId,
                        paramListId, paramListCountId, paramMoneyId, paramTextId, paramTextRegexpId, paramTreeId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props);
    }

    @Test(dependsOnMethods = "addProcessType")
    public void addProcess() throws Exception {
        var p = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);
        Assert.assertTrue(0 < (processId = p.getId()));
    }

    @Test(dependsOnMethods = { "addProcess" })
    public void addParamValue() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        var valueMoney = dao.getParamMoney(processId, paramMoneyId);
        Assert.assertNull(valueMoney);
        dao.updateParamMoney(processId, paramMoneyId, Utils.parseBigDecimal("10.55"));
        valueMoney = dao.getParamMoney(processId, paramMoneyId);
        Assert.assertEquals(Utils.parseBigDecimal("10.55"), valueMoney);
    }
}
