package org.bgerp.itest.kernel.process;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.AddressTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processQueueFilterParam", dependsOnGroups = "process")
public class ProcessQueueFilterParamTest {
    private static final String TITLE = "Kernel Process Queue Filter Param";

    private int paramAddressId;
    private int paramBlobId;
    private int paramDateId;
    private int paramDateTimeId;
    private int paramListId;
    private int paramListCountId;
    private int paramMoneyId;
    private int paramTextId;

    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, TITLE + " addr", ProcessTest.posParam += 2);
        paramBlobId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_BLOB, TITLE + " blob", ProcessTest.posParam += 2);
        paramDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " date", ProcessTest.posParam += 2);
        paramDateTimeId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATETIME, TITLE + " datetime", ProcessTest.posParam += 2);
        paramListId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " list", ProcessTest.posParam += 2, "", ParamTest.LIST_VALUES_123);
        paramListCountId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LISTCOUNT, TITLE + " listcount", ProcessTest.posParam += 2, "", ParamTest.LIST_VALUES_123);
        paramMoneyId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " money", ProcessTest.posParam += 2);
        paramTextId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " text", ProcessTest.posParam += 2);
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(
                List.of(paramAddressId, paramBlobId, paramDateId, paramDateTimeId, paramListId, paramListCountId, paramMoneyId, paramTextId));

       processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
            ConfigHelper.generateConstants(
                "PARAM_ADDRESS_ID", paramAddressId,
                "PARAM_BLOB_ID", paramBlobId,
                "PARAM_DATE_ID", paramDateId,
                "PARAM_DATETIME_ID", paramDateTimeId,
                "PARAM_LIST_ID", paramListId,
                "PARAM_LISTCOUNT_ID", paramListCountId,
                "PARAM_MONEY_ID", paramMoneyId,
                "PARAM_TEXT_ID", paramTextId
            ) + ResourceHelper.getResource(this, "process.queue.config.txt"),
            Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot);

        var process = ProcessHelper.addProcess(processTypeId, TITLE + " param address");
        dao.updateParamAddress(process.getId(), paramAddressId, 0, new ParameterAddressValue().withHouseId(AddressTest.houseUfa6.getId()).withFlat("33"));
        process = ProcessHelper.addProcess(processTypeId, TITLE + " param blob");
        dao.updateParamBlob(process.getId(), paramBlobId, "Blob param value");
        process = ProcessHelper.addProcess(processTypeId, TITLE + " param date");
        dao.updateParamDate(process.getId(), paramDateId, new Date());
        process = ProcessHelper.addProcess(processTypeId, TITLE + " param datetime");
        dao.updateParamDateTime(process.getId(), paramDateTimeId, new Date());
        process = ProcessHelper.addProcess(processTypeId, TITLE + " param list");
        dao.updateParamList(process.getId(), paramListId, Set.of(1));
        process = ProcessHelper.addProcess(processTypeId, TITLE + " param listcount");
        dao.updateParamListCount(process.getId(), paramListCountId, Map.of(2, "42.0", 3, "4.2"));
        process = ProcessHelper.addProcess(processTypeId, TITLE + " param money");
        dao.updateParamMoney(process.getId(), paramMoneyId, "0.42");
        process = ProcessHelper.addProcess(processTypeId, TITLE + " param text");
        dao.updateParamText(process.getId(), paramTextId, "Text param value");
    }
}
