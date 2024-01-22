package org.bgerp.itest.kernel.process;

import java.time.Instant;
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
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;

@Test(groups = "processQueueColumn", dependsOnGroups = "process")
public class ProcessQueueColumnTest {
    private static final String TITLE = "Kernel Process Queue Column";

    private int paramMoneyAmountId;
    private int paramBlobLargeTextId;
    private int paramTextIntId;

    private int processTypeId;

    @Test
    public void param() throws Exception {
        paramMoneyAmountId =  ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " money amount",
            ProcessTest.posParam += 2, "", "");
        paramBlobLargeTextId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_BLOB, TITLE + " long text",
            ProcessTest.posParam += 2, "", "");
        paramTextIntId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " text with int",
            ProcessTest.posParam += 2, "", "");
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramMoneyAmountId, paramBlobLargeTextId, paramTextIntId));

       processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
            ConfigHelper.generateConstants(
                "PARAM_MONEY_AMOUNT_ID", paramMoneyAmountId,
                "PARAM_BLOB_LARGE_TEXT_ID", paramBlobLargeTextId,
                "PARAM_TEXT_INT_ID", paramTextIntId
            ) + ResourceHelper.getResource(this, "process.queue.config.txt"),
            Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot);

        var processFirst = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);
        processFirst.setCloseTime(Date.from(Instant.now().minusSeconds(100)));
        new ProcessDAO(DbTest.conRoot).updateProcess(processFirst);
        dao.updateParamMoney(processFirst.getId(), paramMoneyAmountId, "3.44");
        dao.updateParamBlob(processFirst.getId(), paramBlobLargeTextId, ResourceHelper.getResource(this, "param.blob.large.text.value.1.txt"));
        dao.updateParamText(processFirst.getId(), paramTextIntId, "100500");

        var processSecond = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE);
        processSecond.setCloseTime(new Date());
        new ProcessDAO(DbTest.conRoot).updateProcess(processSecond);
        dao.updateParamMoney(processSecond.getId(), paramMoneyAmountId, "4.0");
        dao.updateParamBlob(processSecond.getId(), paramBlobLargeTextId, ResourceHelper.getResource(this, "param.blob.large.text.value.2.txt"));
        dao.updateParamText(processSecond.getId(), paramTextIntId, "101");
    }
}
