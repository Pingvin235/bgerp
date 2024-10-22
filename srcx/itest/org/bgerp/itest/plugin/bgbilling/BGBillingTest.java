package org.bgerp.itest.plugin.bgbilling;

import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.customer.CustomerRuTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.process.link.ProcessLink;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;

@Test(groups = "bgbilling", priority = 100, dependsOnGroups = { "process", "message", "customerRu" })
public class BGBillingTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();
    private static final String TITLE_HD = TITLE + " HD";

    private static final String BILLING_ID = "bill";

    private int processTypeId;

    private int paramAddressId;
    private int paramAddressTextId;
    private int paramDateId;
    private int paramPhoneId;
    private int paramPhoneTextId;
    private int paramTextId;

    private int paramHdCostId;
    private int paramHdStatusId;
    private int paramHdAutoCloseId;

    private int processHdTypeId;

    @Test
    public void param() throws Exception {
        paramAddressId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_ADDRESS, TITLE + " Адрес", ProcessTest.posParam += 2, "", "");
        paramAddressTextId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " Адрес (текст)", ProcessTest.posParam += 2, "", "");
        paramDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " Дата", ProcessTest.posParam += 2, "", "");
        paramPhoneId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_PHONE, TITLE + " Телефон", ProcessTest.posParam += 2, "", "");
        paramPhoneTextId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " Телефон (текст)", ProcessTest.posParam += 2, "", "");
        paramTextId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " Текст", ProcessTest.posParam += 2, "", "");

        paramHdCostId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE_HD + " Стоимость", ProcessTest.posParam += 2, "", "");
        paramHdStatusId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE_HD + " Статус", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "param.hd.status.values.txt"));
        paramHdAutoCloseId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE_HD + " Автозакрытие", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "param.hd.autoclose.values.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramAddressId, paramAddressTextId, paramDateId, paramPhoneId, paramPhoneTextId, paramTextId));
        props.setGroups(ProcessGroup.toProcessGroupSet(Set.of(UserTest.groupAdminsId), 0));
        props.setConfig(
            ConfigHelper.generateConstants(
                "PARAM_ADDR_ID", paramAddressId,
                "PARAM_ADDR_TEXT_ID", paramAddressTextId,
                "PARAM_DATE_ID", paramDateId,
                "PARAM_PHONE_ID", paramPhoneId,
                "PARAM_PHONE_TEXT_ID", paramPhoneTextId,
                "PARAM_TEXT_ID", paramTextId
            ) + ResourceHelper.getResource(this, "process.type.config.txt")
        );

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, props).getId();

        props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramHdCostId, paramHdStatusId, paramHdAutoCloseId));
        props.setGroups(ProcessGroup.toProcessGroupSet(Set.of(UserTest.groupAdminsId), 0));

        processHdTypeId = ProcessHelper.addType(TITLE_HD, ProcessTest.processTypeTestGroupId, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE,
                ConfigHelper.generateConstants(
                    "BILLING_ID", BILLING_ID
                ) + ResourceHelper.getResource(this, "process.queue.config.txt"), Set.of(processTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));

        queueId = ProcessHelper.addQueue(TITLE_HD,
                ConfigHelper.generateConstants(
                    "BILLING_ID", BILLING_ID,
                    "PARAM_HD_COST_ID", paramHdCostId,
                    "PARAM_HD_STATUS_ID", paramHdStatusId
                ) + ResourceHelper.getResource(this, "process.hd.queue.config.txt"),
                Set.of(processHdTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }

    @Test(dependsOnMethods = "processType")
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "BILLING_ID", BILLING_ID,
                "PROCESS_HD_TYPE_ID", processHdTypeId,
                "PARAM_HD_COST_ID", paramHdCostId,
                "PARAM_HD_STATUS_ID", paramHdStatusId,
                "PARAM_HD_AUTO_CLOSE_ID", paramHdAutoCloseId,
                "PROCESS_HD_OPEN_STATUS_ID", ProcessTest.statusOpenId,
                "PROCESS_HD_CLOSE_STATUS_ID", ProcessTest.statusDoneId,
                "PROCESS_HD_READ_STATUS_IDS", "",
                "PARAM_RU_INN", CustomerRuTest.paramInnId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "processType")
    public void process() throws Exception {
        int processId = ProcessHelper.addProcess(processTypeId, TITLE).getId();
        new ProcessLinkDAO(DbTest.conRoot).addLinkIfNotExist(new ProcessLink(processId, Contract.OBJECT_TYPE + ":" + BILLING_ID, 1, "test"));
        MessageHelper.addHowToTestNoteMessage(processId, this);

        // HD processes have to be imported
    }
}
