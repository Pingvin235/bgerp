package org.bgerp.itest.plugin.bil.invoice;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.customer.CustomerRuTest;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.plugin.bil.invoice.Config;
import org.bgerp.plugin.bil.invoice.Plugin;
import org.bgerp.plugin.bil.invoice.dao.InvoiceDAO;
import org.bgerp.plugin.bil.invoice.model.InvoiceType;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Test(groups = "invoice", priority = 100, dependsOnGroups = { "config", "process", "customer", "customerRu", "message" })
public class InvoiceTest {
    private static final Plugin PLUGIN = Plugin.INSTANCE;
    private static final String TITLE = PLUGIN.getTitleWithPrefix();

    private int paramContractDateId;
    private int paramCostId;

    private Process process;
    private Process processRu;
    private InvoiceType type;
    private InvoiceType typeRu;

    @Test
    public void process() throws Exception {
        paramContractDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Contract date", ProcessTest.posParam += 2, "", "");
        paramCostId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_MONEY, "Cost", ProcessTest.posParam += 2, "", "");

        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.setParameterIds(List.of(paramContractDateId, paramCostId));
        props.setConfig(ResourceHelper.getResource(this, "processType.txt"));

        var paramDao = new ParamValueDAO(DbTest.conRoot);

        var processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();

        process = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Contract EU");
        paramDao.updateParamDate(process.getId(), paramContractDateId, Date.from(Instant.now().plus(Duration.ofDays(-60))));
        paramDao.updateParamMoney(process.getId(), paramCostId, "42.51");
        ProcessHelper.addCustomerLink(process.getId(), Customer.OBJECT_TYPE, CustomerTest.customerOrgNs);

        processRu = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " Contract RU");
        paramDao.updateParamDate(processRu.getId(), paramContractDateId, Date.from(Instant.now().plus(Duration.ofDays(-31))));
        paramDao.updateParamMoney(processRu.getId(), paramCostId, "42.50");
        ProcessHelper.addCustomerLink(processRu.getId(), Customer.OBJECT_TYPE, CustomerRuTest.customerOrgIvan);
    }

    @Test(dependsOnMethods = "process")
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig(PLUGIN,
            ConfigHelper.generateConstants(
                "PARAM_COST_ID", paramCostId,
                "PARAM_CONTRACT_DATE_ID", paramContractDateId,

                "CUSTOMER_ID", CustomerTest.customerOrgNs.getId(),
                "PARAM_CUSTOMER_ADDRESS_ID", CustomerTest.paramAddressId,
                "PARAM_CUSTOMER_BANK_TITLE_ID", CustomerTest.paramBankTitleId,
                "PARAM_CUSTOMER_BANK_IBAN_ID", CustomerTest.paramBankIbanId,
                "PARAM_CUSTOMER_BANK_BIC_ID", CustomerTest.paramBankBicId,
                "PARAM_CUSTOMER_LOGO_ID", CustomerTest.paramLogoId,
                "PARAM_CUSTOMER_INVOICE_FOOTER_ID", CustomerTest.paramInvoiceFooterId,

                "CUSTOMER_RU_ID", CustomerRuTest.customerOrgIvan.getId(),
                "PARAM_CUSTOMER_RU_JUR_ADDRESS_ID", CustomerRuTest.paramJurAddressId,
                "PARAM_CUSTOMER_RU_POST_ADDRESS_ID", CustomerRuTest.paramPostAddressId,
                "PARAM_CUSTOMER_RU_INN_ID", CustomerRuTest.paramInnId,
                "PARAM_CUSTOMER_RU_KPP_ID", CustomerRuTest.paramKppId,
                "PARAM_CUSTOMER_RU_OGRN_ID", CustomerRuTest.paramOgrnId,
                "PARAM_CUSTOMER_RU_BANK_TITLE_ID", CustomerRuTest.paramBankTitleId,
                "PARAM_CUSTOMER_RU_BANK_BIC_ID", CustomerRuTest.paramBankBicId,
                "PARAM_CUSTOMER_RU_BANK_CORR_ACCOUNT_ID", CustomerRuTest.paramBankCorrAccountId,
                "PARAM_CUSTOMER_RU_BANK_ACCOUNT_ID", CustomerRuTest.paramBankAccountId,
                "PARAM_CUSTOMER_RU_SIGN_POST_ID", CustomerRuTest.paramSignPostId,
                "PARAM_CUSTOMER_RU_SIGN_ID", CustomerRuTest.paramSignId,
                "PARAM_CUSTOMER_RU_SIGN_NAME_ID", CustomerRuTest.paramSignNameId,
                "PARAM_CUSTOMER_RU_STAMP_ID", CustomerRuTest.paramStampId,
                "PARAM_CUSTOMER_RU_INVOICE_FOOTER_ID", CustomerRuTest.paramInvoiceFooterId
            ) +
            ResourceHelper.getResource(this, "config.txt"));
        Config config = Setup.getSetup().getConfig(Config.class);
        Assert.assertNotNull(type = config.getType(1));
        Assert.assertNotNull(typeRu = config.getType(2));
    }

    @Test(dependsOnMethods = "config")
    public void position() throws Exception {
        var config = Setup.getSetup().getConfig(Config.class);
        var positions = config.getPositions();
        Assert.assertEquals(2, positions.size());
        var iterator = positions.iterator();
        Assert.assertEquals(iterator.next().getTitle(), "Consultancy");
        Assert.assertEquals(iterator.next().getId(), "test");
    }

    @Test(dependsOnMethods = { "process", "config" })
    public void invoiceEu() throws Exception {
        var invoice = type.invoice(new SingleConnectionSet(DbTest.conRoot), process.getId(), YearMonth.now());
        Assert.assertNotNull(invoice);
        Assert.assertEquals(invoice.getPositions().size(), 1);
        var pos = invoice.getPositions().get(0);
        Assert.assertEquals(pos.getId(), "consultancy");
        Assert.assertEquals(pos.getTitle(), "Consultancy " + YearMonth.now().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.US) + " " + YearMonth.now().getYear());
        Assert.assertEquals(pos.getAmount(), Utils.parseBigDecimal("42.51"));

        var dao = new InvoiceDAO(DbTest.conRoot);
        type.getNumberProvider().number(DbTest.conRoot, type, invoice);

        Assert.assertEquals(invoice.getNumber(),
                "EU" + new DecimalFormat("000000").format(process.getId()) + "-" + TimeUtils.format(invoice.getDateFrom(), "yyyyMM") + "-01");

        dao.update(invoice);
        Assert.assertTrue(invoice.getId() > 0);

        MessageHelper.addNoteMessage(process.getId(), UserTest.USER_ADMIN_ID, Duration.ofSeconds(0), MessageHelper.HOW_TO_TEST_MESSAGE_SUBJECT,
                ResourceHelper.getResource(this, "howto.eu.txt"));
    }

    @Test(dependsOnMethods = { "process", "config" })
    public void invoiceRu() throws Exception {
        var invoice = typeRu.invoice(new SingleConnectionSet(DbTest.conRoot), processRu.getId(), YearMonth.now());
        Assert.assertNotNull(invoice);
        Assert.assertEquals(invoice.getPositions().size(), 1);
        var pos = invoice.getPositions().get(0);
        Assert.assertEquals(pos.getId(), "consultancy");
        Assert.assertTrue(pos.getTitle().startsWith("Консультационно-справочное обслуживание"));
        Assert.assertEquals(pos.getAmount(), Utils.parseBigDecimal("42.50"));

        var dao = new InvoiceDAO(DbTest.conRoot);
        typeRu.getNumberProvider().number(DbTest.conRoot, typeRu, invoice);

        Assert.assertEquals(invoice.getNumber(),
                "RU" + new DecimalFormat("000000").format(processRu.getId()) + "-" + TimeUtils.format(invoice.getDateFrom(), "yyyyMM") + "-01");

        dao.update(invoice);
        Assert.assertTrue(invoice.getId() > 0);

        MessageHelper.addNoteMessage(process.getId(), UserTest.USER_ADMIN_ID, Duration.ofSeconds(0), MessageHelper.HOW_TO_TEST_MESSAGE_SUBJECT,
                ResourceHelper.getResource(this, "howto.ru.txt"));
    }
}