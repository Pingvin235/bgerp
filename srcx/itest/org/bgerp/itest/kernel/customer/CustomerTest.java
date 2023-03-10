package org.bgerp.itest.kernel.customer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.CustomerHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.io.IOUtils;

@Test(groups = "customer", dependsOnGroups = "param")
public class CustomerTest {
    // defined in CustomerTest.config.txt
    public static final String LINK_TYPE_CUSTOMER = Customer.OBJECT_TYPE;
    public static final String LINK_TYPE_CONTACT = Customer.OBJECT_TYPE + "-contact";

    public static volatile int posParam;

    public static volatile int paramEmailId;
    public static volatile int paramPhoneId;

    private int paramBirthDateId;
    private int paramBirthPlaceId;
    public static volatile int paramAddressId;
    private int paramReligionId;
    public static volatile int paramServiceAddressId;

    public static volatile int paramBankTitleId;
    public static volatile int paramBankBicId;
    public static volatile int paramBankIbanId;

    public static volatile int paramLogoId;

    public static volatile int paramInvoiceFooterId;

    private int paramOrgTitleId;
    private int paramOrgFormId;

    private int titlePatternOrgId;
    private String titlePatternOrgPattern;

    private int paramGroupOrgId;
    public static volatile int paramGroupPersonId;

    public static final String CUSTOMER_ORG_NS_DOMAIN = "muster.com";
    public static final String CUSTOMER_ORG_NS_TILL_MAIL = "muster@" + CUSTOMER_ORG_NS_DOMAIN;
    public static final String CUSTOMER_ORG_NS_TILL_NAME = "Max Mustermann";
    public static volatile Customer customerOrgNs;

    public static final String CUSTOMER_PERS_IVAN_MAIL = "ivan@bgerp.org";
    public static final String CUSTOMER_PERS_IVAN_NAME = "Ivan Drago";
    public static final String CUSTOMER_PERS_IVAN_PHONE = "7917737373";
    public static volatile Customer customerPersonIvan;

    @Test
    public void param() throws Exception {
        paramEmailId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_EMAIL, "Email(s)", posParam += 2, ParamTest.MULTIPLE, "");
        paramPhoneId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_PHONE, "Phone number", posParam += 2 , "", "");

        // TODO: Make date chooser configuration.
        paramBirthDateId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_DATE, "Birth date", posParam += 2, "", "");
        paramBirthPlaceId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Birth place", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Address", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramReligionId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Religion", posParam += 2, ParamTest.ENCRYPTED, "");
        paramServiceAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_ADDRESS, "Service address", posParam += 2, ParamTest.MULTIPLE, "");

        paramBankTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Bank", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankBicId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Bank BIC", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankIbanId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "IBAN", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");

        paramLogoId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_FILE, "Logo", posParam += 2, "", "");

        paramInvoiceFooterId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_BLOB, "Invoice Bottom Text", CustomerTest.posParam += 2,
                "", "");

        paramOrgTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Organization title", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramOrgFormId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_LIST, "Organization form", posParam += 2, "",
                ResourceHelper.getResource(this, "orgforms.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig("Kernel Customer",
            ConfigHelper.generateConstants(
                "PARAM_BIRTH_DATE_ID", paramBirthDateId,
                "PARAM_BIRTH_PLACE_ID", paramBirthPlaceId,
                "PARAM_SERVICE_ADDRESS_ID", paramServiceAddressId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void paramGroup() throws Exception {
        paramGroupOrgId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Organization", Set.of(
                paramEmailId, paramPhoneId, paramOrgFormId, paramOrgTitleId, paramAddressId,
                paramBankTitleId, paramBankIbanId, paramBankBicId,
                paramServiceAddressId,
                paramLogoId,
                paramInvoiceFooterId));
        paramGroupPersonId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Person",
                Set.of(paramEmailId, paramPhoneId, paramBirthDateId, paramBirthPlaceId, paramReligionId, paramAddressId, paramServiceAddressId));
    }

    @Test(dependsOnMethods = "param")
    public void patternTitle() throws Exception {
        titlePatternOrgPattern = "${param_" + paramOrgTitleId + "} ${param_" + paramOrgFormId + "}";
        titlePatternOrgId = ParamHelper.addPattern(Customer.OBJECT_TYPE, "Organization", titlePatternOrgPattern);
        // TODO: Pattern for person with birthday date.
    }

    @Test(dependsOnMethods = "patternTitle")
    public void customer() throws Exception {
        var con = DbTest.conRoot;

        var paramDao = new ParamValueDAO(con);

        // Ivan Drago
        customerPersonIvan = CustomerHelper.addCustomer(0, paramGroupPersonId, CUSTOMER_PERS_IVAN_NAME);

        int customerId = customerPersonIvan.getId();
        paramDao.updateParamEmail(customerId, paramEmailId, 0, new ParameterEmailValue(CUSTOMER_PERS_IVAN_MAIL));
        paramDao.updateParamPhone(customerId, paramPhoneId, new ParameterPhoneValue(List.of(new ParameterPhoneValueItem(CUSTOMER_PERS_IVAN_PHONE, "13", ""))));
        paramDao.updateParamDate(customerId, paramBirthDateId, new GregorianCalendar(1983, Calendar.JULY, 1).getTime());
        paramDao.updateParamText(customerId, paramBirthPlaceId, "Uzgala village");
        paramDao.updateParamText(customerId, paramAddressId, "Lenina Street 1, 450000, Ufa Russia");

        // Muster Gmbh
        customerOrgNs = CustomerHelper.addCustomer(titlePatternOrgId, paramGroupOrgId, "");

        customerId = customerOrgNs.getId();
        paramDao.updateParamEmail(customerId, paramEmailId, 0, new ParameterEmailValue(CUSTOMER_ORG_NS_TILL_MAIL, CUSTOMER_ORG_NS_TILL_NAME));
        paramDao.updateParamEmail(customerId, paramEmailId, 0, new ParameterEmailValue(CUSTOMER_ORG_NS_DOMAIN, "Domain"));
        paramDao.updateParamPhone(customerId, paramPhoneId, new ParameterPhoneValue(List.of(new ParameterPhoneValueItem("666", "", ""))));
        paramDao.updateParamText(customerId, paramOrgTitleId, "Muster");
        paramDao.updateParamList(customerId, paramOrgFormId, Set.of(4));
        paramDao.updateParamText(customerId, paramAddressId, "Musterstraße 12, 12345 Musterstadt, Germany");

        paramDao.updateParamText(customerId, paramBankTitleId, "Sparkasse Musterstadt");
        paramDao.updateParamText(customerId, paramBankIbanId, "DE00 0000 0000 0000 0000 00");
        paramDao.updateParamText(customerId, paramBankBicId, "DEUTDESMXXX");

        paramDao.updateParamFile(customerId, paramLogoId, 0, new FileData("logo.svg", IOUtils.read("webapps/img/favicon.svg")));
        paramDao.updateParamFile(customerId, paramLogoId, 0, new FileData("logo.png", ResourceHelper.getResourceBytes(this, "logo.png")));

        paramDao.updateParamBlob(customerId, paramInvoiceFooterId, ResourceHelper.getResource(this, "invoice.footer.txt"));

        var customerDao = new CustomerDAO(con);
        customerOrgNs.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customerId, paramDao, titlePatternOrgPattern));
        customerDao.updateCustomer(customerOrgNs);
    }
}
