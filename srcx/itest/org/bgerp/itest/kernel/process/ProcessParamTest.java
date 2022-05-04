package org.bgerp.itest.kernel.process;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.Pageable;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.ParamLogDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.FileData;
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
    public void param() throws Exception {
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

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.getParameterIds().addAll(List.of(paramAddressId, paramDateId, paramDatetimeId, paramEmailId, paramFileId,
                        paramListId, paramListCountId, paramMoneyId, paramTextId, paramTextRegexpId, paramTreeId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var p = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);
        Assert.assertTrue(0 < (processId = p.getId()));
    }

    @Test(dependsOnMethods = "process")
    public void paramValueFile() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());
        var logDao = new ParamLogDAO(DbTest.conRoot);

        var valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertTrue(valueFile.values().isEmpty());

        dao.updateParamFile(processId, paramFileId, 0, new FileData("file1.txt",
                IOUtils.toByteArray(this.getClass().getResourceAsStream(this.getClass().getSimpleName() + ".param.file.value.txt"))));
        dao.updateParamFile(processId, paramFileId, 0, new FileData("file2.txt",
                IOUtils.toByteArray(this.getClass().getResourceAsStream(this.getClass().getSimpleName() + ".param.file.value.txt"))));
        valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertEquals(valueFile.size(), 2);
        Assert.assertEquals(valueFile.get(1).getTitle(), "file1.txt");
        byte[] data1 = IOUtils.toByteArray(new FileDataDAO(DbTest.conRoot).getFile(valueFile.get(1)).toURI());
        Assert.assertEquals(new String(data1, StandardCharsets.UTF_8).trim(), "Test content");
        Assert.assertEquals(valueFile.get(2).getTitle(), "file2.txt");

        dao.updateParamFile(processId, paramFileId, 1, null);
        valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertEquals(valueFile.size(), 1);

        dao.updateParamFile(processId, paramFileId, -1, null);
        valueFile = dao.getParamFile(processId, paramFileId);
        Assert.assertTrue(valueFile.isEmpty());

        var log = logDao.getHistory(processId, ParameterCache.getParameterList(List.of(paramFileId)), false, new Pageable<>());
        Assert.assertEquals(log.size(), 4);
        Assert.assertEquals(log.get(3).getText(), "file1.txt");
        Assert.assertEquals(log.get(2).getText(), "file1.txt, file2.txt");
        Assert.assertEquals(log.get(1).getText(), "file2.txt");
        Assert.assertEquals(log.get(0).getText(), "");
    }

    @Test(dependsOnMethods = "process")
    public void paramValueMoney() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot, true, User.USER_SYSTEM.getId());

        var valueMoney = dao.getParamMoney(processId, paramMoneyId);
        Assert.assertNull(valueMoney);
        dao.updateParamMoney(processId, paramMoneyId, Utils.parseBigDecimal("10.55"));
        valueMoney = dao.getParamMoney(processId, paramMoneyId);
        Assert.assertEquals(valueMoney, Utils.parseBigDecimal("10.55"));

        // TODO: Check history logs.
    }
}
