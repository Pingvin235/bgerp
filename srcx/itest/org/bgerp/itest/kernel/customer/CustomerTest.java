package org.bgerp.itest.kernel.customer;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
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
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.util.Utils;

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
    private int paramAddressId;
    private int paramReligionId;
    private int paramServiceAddressId;

    private int paramOrgTitleId;
    private int paramOrgFormId;

    private int titlePatternOrgId;
    private String titlePatternOrgPattern;

    private int paramGroupOrgId;
    private int paramGroupPersonId;

    public static final String CUSTOMER_ORG_NS_DOMAIN = "nicrosoft.com";
    public static final String CUSTOMER_ORG_NS_TILL_MAIL = "till@" + CUSTOMER_ORG_NS_DOMAIN;
    public static final String CUSTOMER_ORG_NS_TILL_NAME = "Till Gates";
    public static volatile Customer customerOrgNs;

    public static final String CUSTOMER_PERS_IVAN_MAIL = "ivan@bgerp.org";
    public static final String CUSTOMER_PERS_IVAN_NAME = "Ivan Drago";
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

        paramOrgTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Organization title", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramOrgFormId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_LIST, "Organization form", posParam += 2, "",
                ResourceHelper.getResource(this, "orgforms.txt"));

        // IBAN, BIC - with validation
    }

    @Test(dependsOnMethods = "param")
    public void config() throws Exception {
        ConfigHelper.addIncludedConfig("Kernel Customer",
            ConfigHelper.generateConstants(
                "PARAM_BIRTH_DATE_ID", paramBirthDateId,
                "PARAM_BIRTH_PLACE_ID", paramBirthPlaceId
            ) + ResourceHelper.getResource(this, "config.txt"));
    }

    @Test(dependsOnMethods = "param")
    public void paramGroup() throws Exception {
        paramGroupOrgId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Organization",
                Set.of(paramEmailId, paramPhoneId, paramOrgFormId, paramOrgTitleId, paramAddressId, paramServiceAddressId));
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

        customerPersonIvan = CustomerHelper.addCustomer(0, paramGroupPersonId, CUSTOMER_PERS_IVAN_NAME);

        int customerId = customerPersonIvan.getId();
        paramDao.updateParamEmail(customerId, paramEmailId, 0, new ParameterEmailValue(CUSTOMER_PERS_IVAN_MAIL));
        paramDao.updateParamDate(customerId, paramBirthDateId, new GregorianCalendar(1983, Calendar.JULY, 1).getTime());
        paramDao.updateParamText(customerId, paramBirthPlaceId, "Uzgala village");
        paramDao.updateParamText(customerId, paramAddressId, "Lenina Street 1, 450000, Ufa Russia");

        customerOrgNs = CustomerHelper.addCustomer(titlePatternOrgId, paramGroupOrgId, "");

        paramDao.updateParamEmail(customerOrgNs.getId(), paramEmailId, 0, new ParameterEmailValue(CUSTOMER_ORG_NS_TILL_MAIL, CUSTOMER_ORG_NS_TILL_NAME));
        paramDao.updateParamEmail(customerOrgNs.getId(), paramEmailId, 0, new ParameterEmailValue(CUSTOMER_ORG_NS_DOMAIN, "Domain"));
        var phoneParamVal = new ParameterPhoneValue();
        phoneParamVal.addItem(new ParameterPhoneValueItem("666", "", ""));
        paramDao.updateParamPhone(customerOrgNs.getId(), paramPhoneId, phoneParamVal);
        paramDao.updateParamText(customerOrgNs.getId(), paramOrgTitleId, "NicroSoft");
        paramDao.updateParamList(customerOrgNs.getId(), paramOrgFormId, Collections.singleton(5));

        var customerDao = new CustomerDAO(con);
        customerOrgNs.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customerOrgNs.getId(), paramDao, titlePatternOrgPattern));
        customerDao.updateCustomer(customerOrgNs);
    }
}
