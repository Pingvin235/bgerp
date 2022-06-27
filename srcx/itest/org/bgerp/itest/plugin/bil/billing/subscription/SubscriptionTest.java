package org.bgerp.itest.plugin.bil.billing.subscription;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.bil.billing.subscription.Config;
import org.bgerp.plugin.bil.billing.subscription.Plugin;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessLinkProcess;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Test(groups = "subscription", priority = 100, dependsOnGroups = { "config", "process", "openIface" })
public class SubscriptionTest {
    private static final Plugin PLUGIN = new Plugin();
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int paramEmailId;
    private int paramSubscriptionId;
    private int paramLimitId;
    private int paramSubscriptionCostId;
    private int paramDateFromId;
    private int paramDateToId;
    private int paramDiscountId;
    private int paramLicFileId;

    private int paramProductId;
    private int paramPriceRubId;
    private int paramPriceEurId;

    // subscription process type
    private int processSubscriptionTypeId;
    private int processProductTypeId;

    // also mentioned in limit.values.txt
    private static final int LIMIT_VALUE_10 = 1;
    private static final int LIMIT_VALUE_UNLIM = 2;

    // also mentioned in config.txt
    private static final int SUBSCRIPTION_RUB = 1;
    private static final int SUBSCRIPTION_EUR = 2;

    @Test
    public void param() throws Exception {
        paramEmailId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_EMAIL, TITLE + " E-Mail", ProcessTest.posParam += 2, "",
                "");
        paramSubscriptionId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " Subscription", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "subscription.values.txt"));
        paramLimitId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " Limit", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "limit.values.txt"));
        paramSubscriptionCostId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " Subscription Cost",
                ProcessTest.posParam += 2, ParamTest.READONLY, "");
        paramDateFromId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " Subscription Date From", ProcessTest.posParam += 2,
                "", "");
        paramDateToId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " Subscription Date To", ProcessTest.posParam += 2, "",
                "");
        paramDiscountId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " Subscription Discount", ProcessTest.posParam += 2,
                "", "");
        paramLicFileId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_FILE, TITLE + " Subscription License File",
                ProcessTest.posParam += 2, ParamTest.READONLY, "");

        paramProductId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_TEXT, TITLE + " Product ID", ProcessTest.posParam += 2, "", "");
        paramPriceRubId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LISTCOUNT, TITLE + " Product Price RUB", ProcessTest.posParam += 2,
                "", ResourceHelper.getResource(this, "limit.values.txt"));
        paramPriceEurId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LISTCOUNT, TITLE + " Product Price EUR", ProcessTest.posParam += 2,
                "", ResourceHelper.getResource(this, "limit.values.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "processSubscriptionType.txt"));
        props.setParameterIds(List.of(paramEmailId, paramSubscriptionId, paramLimitId, paramDateFromId, paramDateToId,
                paramDiscountId, paramSubscriptionCostId, paramLicFileId));
        processSubscriptionTypeId = ProcessHelper.addType(TITLE + " Subscription", ProcessTest.processTypeTestGroupId, false, props).getId();

        props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "processProductType.txt"));
        props.setParameterIds(List.of(paramProductId, paramPriceRubId, paramPriceEurId));
        processProductTypeId = ProcessHelper.addType(TITLE + " Product", ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "PROCESS_SUBSCRIPTION_TYPE_ID", processSubscriptionTypeId,
                "PARAM_PRODUCT_ID", paramProductId,
                "PARAM_LIMIT_PRICE_RUB_ID", paramPriceRubId,
                "PARAM_LIMIT_PRICE_EUR_ID", paramPriceEurId,
                "PARAM_LIMIT_ID", paramLimitId,
                "PARAM_EMAIL_ID", paramEmailId,
                "PARAM_SUBSCRIPTION_ID", paramSubscriptionId,
                "PARAM_LIC_FILE_ID", paramLicFileId,
                "PARAM_DATE_FROM_ID", paramDateFromId,
                "PARAM_DATE_TO_ID", paramDateToId,
                "PARAM_COST_DISCOUNT_ID", paramDiscountId,
                "PARAM_COST_ID", paramSubscriptionCostId,
                "PROCESS_PRODUCT_TYPE_ID", processProductTypeId
            ) +
            ResourceHelper.getResource(this, "config.txt"));

        var config = Setup.getSetup().getConfig(Config.class);

        var type = config.getSubscription(1);
        Assert.assertNotNull(type);
        Assert.assertEquals(type.getTitle(), "BGERP RUB");

        type = config.getSubscription(2);
        Assert.assertNotNull(type);
        Assert.assertEquals(type.getTitle(), "BGERP EUR");
    }

    @Test(dependsOnMethods = "config")
    public void process() throws Exception {
        var paramDao = new ParamValueDAO(DbTest.conRoot);

        var processProduct1Id = ProcessHelper.addProcess(processProductTypeId, User.USER_SYSTEM_ID, TITLE + " Product 1").getId();
        paramDao.updateParamText(processProduct1Id, paramProductId, "product1");
        paramDao.updateParamListCount(processProduct1Id, paramPriceRubId, Map.of(
                LIMIT_VALUE_10, Utils.parseBigDecimal("200"),
                LIMIT_VALUE_UNLIM, Utils.parseBigDecimal("300")
        ));
        paramDao.updateParamListCount(processProduct1Id, paramPriceEurId, Map.of(
                LIMIT_VALUE_10, Utils.parseBigDecimal("1"),
                LIMIT_VALUE_UNLIM, Utils.parseBigDecimal("3")
        ));

        var processProduct2Id = ProcessHelper.addProcess(processProductTypeId, User.USER_SYSTEM_ID, TITLE + " Product 2").getId();
        paramDao.updateParamText(processProduct2Id, paramProductId, "product2");
        paramDao.updateParamListCount(processProduct2Id, paramPriceRubId, Map.of(
                LIMIT_VALUE_10, Utils.parseBigDecimal("250"),
                LIMIT_VALUE_UNLIM, Utils.parseBigDecimal("350")
        ));
        paramDao.updateParamListCount(processProduct2Id, paramPriceEurId, Map.of(
                LIMIT_VALUE_10, Utils.parseBigDecimal("1.15"),
                LIMIT_VALUE_UNLIM, Utils.parseBigDecimal("3.24")
        ));

        var processSubscriptionRubId = ProcessHelper.addProcess(processSubscriptionTypeId, User.USER_SYSTEM_ID, TITLE + " Subscription RUB").getId();
        paramDao.updateParamList(processSubscriptionRubId, paramSubscriptionId, Set.of(SUBSCRIPTION_RUB));
        paramDao.updateParamList(processSubscriptionRubId, paramLimitId, Set.of(LIMIT_VALUE_10));
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct1Id, processSubscriptionRubId));
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct2Id, processSubscriptionRubId));
        // trigger cost recalculation
        EventProcessor.processEvent(
                        new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramLimitId),
                                        processSubscriptionRubId, null),
                        new SingleConnectionSet(DbTest.conRoot));
        var cost = paramDao.getParamMoney(processSubscriptionRubId, paramSubscriptionCostId);
        Assert.assertEquals(cost, Utils.parseBigDecimal("450.00"));

        var processSubscriptionEurId = ProcessHelper.addProcess(processSubscriptionTypeId, User.USER_SYSTEM_ID, TITLE + " Subscription EUR").getId();
        paramDao.updateParamList(processSubscriptionEurId, paramSubscriptionId, Set.of(SUBSCRIPTION_EUR));
        paramDao.updateParamList(processSubscriptionEurId, paramLimitId, Set.of(LIMIT_VALUE_UNLIM));
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct1Id, processSubscriptionEurId));
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct2Id, processSubscriptionEurId));
        // trigger cost recalculation
        EventProcessor.processEvent(
                        new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramLimitId),
                                        processSubscriptionEurId, null),
                        new SingleConnectionSet(DbTest.conRoot));
        cost = paramDao.getParamMoney(processSubscriptionEurId, paramSubscriptionCostId);
        Assert.assertEquals(cost, Utils.parseBigDecimal("6.24"));
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int queueId = ProcessHelper.addQueue(TITLE + " Product",
                ConfigHelper.generateConstants(
                    "PARAM_PRODUCT_ID", paramProductId,
                    "PARAM_PRICE_RUB_ID", paramPriceRubId,
                    "PARAM_PRICE_EUR_ID", paramPriceEurId) +
                    ResourceHelper.getResource(this, "processQueue.txt"),
                Set.of(processProductTypeId));
        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(queueId));
    }
}