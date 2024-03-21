package org.bgerp.itest.plugin.bil.subscription;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.Setup;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.CustomerHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.process.link.ProcessLinkProcess;
import org.bgerp.plugin.bil.subscription.Config;
import org.bgerp.plugin.bil.subscription.Plugin;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Test(groups = "subscription", priority = 100, dependsOnGroups = { "config", "process", "openIface", "message", "user", "customer" })
public class SubscriptionTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    // also mentioned in limit.values.txt
    private static final int LIMIT_VALUE_10 = 1;
    private static final int LIMIT_VALUE_UNLIM = 2;

    // also mentioned in config.txt
    private static final int SUBSCRIPTION_RUB = 1;
    private static final int SUBSCRIPTION_EUR = 2;

    // user params
    private int paramUserIncomingTaxPercentId;

    // subscription process params
    private int paramEmailId;
    private int paramSubscriptionId;
    private int paramLimitId;
    private int paramSubscriptionCostId;
    private int paramServiceCostId;
    private int paramDiscountId;
    private int paramDateToId;
    private int paramLicFileId;

    // product process params
    private int paramProductId;
    private int paramPriceRubId;
    private int paramPriceEurId;

    // subscription process type
    private int processSubscriptionTypeId;
    private int processProductTypeId;

    // groups and users
    private int userGroupOwnersId;
    private int userOwner1Id;
    private int userOwner2Id;

    private int userGroupConsultantsId;
    private int userConsultantRuId;
    private int userConsultantEnId;

    // customer
    private Customer customer;

    @Test
    public void param() throws Exception {
        paramUserIncomingTaxPercentId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " Incoming Tax Percent", UserTest.posParam += 2, "",
                "");

        paramEmailId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_EMAIL, TITLE + " E-Mail", ProcessTest.posParam += 2, "",
                "");
        paramSubscriptionId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " Subscription", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "subscription.values.txt"));
        paramLimitId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " Limit", ProcessTest.posParam += 2, "",
                ResourceHelper.getResource(this, "limit.values.txt"));
        paramServiceCostId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " Service Cost", ProcessTest.posParam += 2,
                "", "");
        paramDiscountId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " Discount", ProcessTest.posParam += 2,
                "", "");
        paramSubscriptionCostId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, TITLE + " Subscription Cost",
                ProcessTest.posParam += 2, ParamTest.READONLY, "");
        paramDateToId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, TITLE + " Subscription Date To", ProcessTest.posParam += 2, "",
                "");
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
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ConfigHelper.generateConstants("PARAM_COST_ID", paramSubscriptionCostId) +
            ResourceHelper.getResource(this, "process.subscription.type.config.txt"));
        props.setParameterIds(List.of(paramEmailId, paramSubscriptionId, paramLimitId, paramServiceCostId,
                paramDiscountId, paramSubscriptionCostId, paramDateToId, paramLicFileId));
        processSubscriptionTypeId = ProcessHelper.addType(TITLE + " Subscription", ProcessTest.processTypeTestGroupId, false, props).getId();

        props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatusId(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setConfig(ResourceHelper.getResource(this, "process.product.type.config.txt"));
        props.setParameterIds(List.of(paramProductId, paramPriceRubId, paramPriceEurId));
        processProductTypeId = ProcessHelper.addType(TITLE + " Product", ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test(dependsOnMethods = "processType")
    public void processQueue() throws Exception {
        int productQueueId = ProcessHelper.addQueue(TITLE + " Product",
            ConfigHelper.generateConstants(
                "PARAM_PRODUCT_ID", paramProductId,
                "PARAM_PRICE_RUB_ID", paramPriceRubId,
                "PARAM_PRICE_EUR_ID", paramPriceEurId
            ) + ResourceHelper.getResource(this, "process.queue.product.config.txt"),
            Set.of(processProductTypeId));

        int subscriptionQueueId = ProcessHelper.addQueue(TITLE + " Subscription",
            ConfigHelper.generateConstants(
                "PARAM_SUBSCRIPTION_ID", paramSubscriptionId,
                "PARAM_DATE_TO_ID", paramDateToId
            ) + ResourceHelper.getResource(this, "process.queue.subscription.config.txt"),
            Set.of(processSubscriptionTypeId));

        UserHelper.addUserProcessQueues(UserTest.USER_ADMIN_ID, Set.of(productQueueId, subscriptionQueueId));
    }

    @Test(dependsOnMethods = "processType")
    public void config() throws Exception {
        ConfigHelper.addPluginConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "PROCESS_SUBSCRIPTION_TYPE_ID", processSubscriptionTypeId,
                "PARAM_PRODUCT_ID", paramProductId,
                "PARAM_USER_TAX_PERCENT", paramUserIncomingTaxPercentId,
                "PARAM_LIMIT_PRICE_RUB_ID", paramPriceRubId,
                "PARAM_LIMIT_PRICE_EUR_ID", paramPriceEurId,
                "PARAM_LIMIT_ID", paramLimitId,
                "PARAM_EMAIL_ID", paramEmailId,
                "PARAM_SUBSCRIPTION_ID", paramSubscriptionId,
                "PARAM_LIC_FILE_ID", paramLicFileId,
                "PARAM_DATE_TO_ID", paramDateToId,
                "PARAM_COST_SERVICE_ID", paramServiceCostId,
                "PARAM_COST_DISCOUNT_ID", paramDiscountId,
                "PARAM_COST_ID", paramSubscriptionCostId,
                "PROCESS_PRODUCT_TYPE_ID", processProductTypeId
            ) + ResourceHelper.getResource(this, "config.txt"));

        var config = Setup.getSetup().getConfig(Config.class);

        var type = config.getSubscriptionOrThrow(1);
        Assert.assertNotNull(type);
        Assert.assertEquals(type.getTitle(), "BGERP RUB");

        type = config.getSubscriptionOrThrow(2);
        Assert.assertNotNull(type);
        Assert.assertEquals(type.getTitle(), "BGERP EUR");
    }

    @Test(dependsOnMethods = "param")
    public void user() throws Exception {
        userGroupOwnersId = UserHelper.addGroup(TITLE + " Owners", 0, "");
        userOwner1Id = UserHelper.addUser(TITLE + " Product Owner 1", "su-po1", List.of(new UserGroup(userGroupOwnersId, new Date(), null))).getId();
        userOwner2Id = UserHelper.addUser(TITLE + " Product Owner 2", "su-po2", List.of(new UserGroup(userGroupOwnersId, new Date(), null))).getId();

        userGroupConsultantsId = UserHelper.addGroup(TITLE + " Consultants", 0, "");
        userConsultantRuId = UserHelper.addUser(TITLE + " Consultant RU", "su-sc-ru", List.of(new UserGroup(userGroupConsultantsId, new Date(), null))).getId();
        userConsultantEnId = UserHelper.addUser(TITLE + " Consultant EN", "su-sc-en", List.of(new UserGroup(userGroupConsultantsId, new Date(), null))).getId();

        var dao = new ParamValueDAO(DbTest.conRoot);
        dao.updateParamMoney(UserTest.USER_ADMIN_ID, paramUserIncomingTaxPercentId, "6");
    }

    @Test
    public void customer() throws Exception {
        customer = CustomerHelper.addCustomer(-1, 0, TITLE + " Customer");
    }

    @Test(dependsOnMethods = { "config", "user", "customer" })
    public void process() throws Exception {
        var paramDao = new ParamValueDAO(DbTest.conRoot);
        var processDao = new ProcessDAO(DbTest.conRoot);

        var processProduct1Id = ProcessHelper.addProcess(processProductTypeId, User.USER_SYSTEM_ID, TITLE + " Product 1").getId();
        processDao.updateProcessGroups(Set.of(new ProcessGroup(userGroupOwnersId, 1)), processProduct1Id);
        processDao.updateProcessExecutors(Set.of(new ProcessExecutor(userOwner1Id, userGroupOwnersId, 1)), processProduct1Id);
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
        processDao.updateProcessGroups(Set.of(new ProcessGroup(userGroupOwnersId, 1)), processProduct2Id);
        processDao.updateProcessExecutors(Set.of(new ProcessExecutor(userOwner2Id, userGroupOwnersId, 1)), processProduct2Id);
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
        processDao.updateProcessGroups(Set.of(new ProcessGroup(userGroupConsultantsId)), processSubscriptionRubId);
        processDao.updateProcessExecutors(Set.of(new ProcessExecutor(userConsultantRuId, userGroupConsultantsId)), processSubscriptionRubId);
        paramDao.updateParamEmail(processSubscriptionRubId, paramEmailId, 0, new ParameterEmailValue("testclient-ru@bgerp.org"));
        paramDao.updateParamList(processSubscriptionRubId, paramSubscriptionId, Set.of(SUBSCRIPTION_RUB));
        paramDao.updateParamList(processSubscriptionRubId, paramLimitId, Set.of(LIMIT_VALUE_10));
        paramDao.updateParamMoney(processSubscriptionRubId, paramServiceCostId, new BigDecimal("43.43"));
        paramDao.updateParamMoney(processSubscriptionRubId, paramDiscountId, new BigDecimal("22.01"));
        ProcessHelper.addCustomerLink(processSubscriptionRubId, Customer.OBJECT_TYPE, customer);
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct1Id, processSubscriptionRubId));
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct2Id, processSubscriptionRubId));
        // trigger cost recalculation
        EventProcessor.processEvent(
                        new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramLimitId),
                                        processSubscriptionRubId, null),
                        new SingleConnectionSet(DbTest.conRoot));
        var cost = paramDao.getParamMoney(processSubscriptionRubId, paramSubscriptionCostId);
        Assert.assertEquals(cost, Utils.parseBigDecimal("471.42"));

        MessageHelper.addHowToTestNoteMessage(processSubscriptionRubId, this);

        var processSubscriptionEurId = ProcessHelper.addProcess(processSubscriptionTypeId, User.USER_SYSTEM_ID, TITLE + " Subscription EUR").getId();
        processDao.updateProcessGroups(Set.of(new ProcessGroup(userGroupConsultantsId)), processSubscriptionEurId);
        processDao.updateProcessExecutors(Set.of(new ProcessExecutor(userConsultantEnId, userGroupConsultantsId)), processSubscriptionEurId);
        paramDao.updateParamEmail(processSubscriptionEurId, paramEmailId, 0, new ParameterEmailValue("testclient-eu@bgerp.org"));
        paramDao.updateParamList(processSubscriptionEurId, paramSubscriptionId, Set.of(SUBSCRIPTION_EUR));
        paramDao.updateParamList(processSubscriptionEurId, paramLimitId, Set.of(LIMIT_VALUE_UNLIM));
        paramDao.updateParamMoney(processSubscriptionEurId, paramServiceCostId, new BigDecimal("4.50"));
        paramDao.updateParamMoney(processSubscriptionEurId, paramDiscountId, new BigDecimal("2.10"));
        ProcessHelper.addCustomerLink(processSubscriptionEurId, Customer.OBJECT_TYPE, customer);
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct1Id, processSubscriptionEurId));
        ProcessHelper.addLink(new ProcessLinkProcess.Depend(processProduct2Id, processSubscriptionEurId));
        // trigger cost recalculation
        EventProcessor.processEvent(
                        new ParamChangedEvent(DynActionForm.SYSTEM_FORM, ParameterCache.getParameter(paramLimitId),
                                        processSubscriptionEurId, null),
                        new SingleConnectionSet(DbTest.conRoot));
        cost = paramDao.getParamMoney(processSubscriptionEurId, paramSubscriptionCostId);
        Assert.assertEquals(cost, Utils.parseBigDecimal("8.64"));

        MessageHelper.addHowToTestNoteMessage(processSubscriptionEurId, this);
    }
}