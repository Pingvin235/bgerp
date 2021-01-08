package org.bgerp.itest.kernel.customer;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import com.google.common.collect.Sets;

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
import ru.bgcrm.util.Utils;

@Test(groups = "customer", dependsOnGroups = "param")
public class CustomerTest {
    // defined in CustomerTest.config.txt
    public static final String LINK_TYPE_CUSTOMER = Customer.OBJECT_TYPE;
    public static final String LINK_TYPE_CONTACT = Customer.OBJECT_TYPE + "-contact";

    public static volatile int paramEmailId;
    
    private int paramBirthDateId;
    private int paramBirthPlaceId;
    private int paramLivingAddressId;
    
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
    public void addConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Kernel Customer", ResourceHelper.getResource(this, "config.txt"));
    }

    @Test
    public void addParams() throws Exception {
        int pos = 0;
        paramEmailId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_EMAIL, "Email(s)", pos += 2, ParamTest.MULTIPLE, "");
        
        // TODO: Make date chooser configuration.
        paramBirthDateId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_DATE, "Birth date", pos += 2, "", "");
        paramBirthPlaceId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Birth place", pos += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramLivingAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Residential address", pos += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        
        paramOrgTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Organization title", pos += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramOrgFormId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_LIST, "Organization form", pos += 2, "", ResourceHelper.getResource(this, "orgforms.txt"));
        
        // IBAN, BIC - with validation
        
        // Russian INN, KPP
    }
    
    @Test(dependsOnMethods = "addParams")
    public void addParamGroups() throws Exception {
        paramGroupOrgId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Organization", Sets.newHashSet(paramEmailId, paramOrgFormId, paramOrgTitleId));
        paramGroupPersonId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Person", Sets.newHashSet(paramEmailId, paramBirthDateId, paramBirthPlaceId, paramLivingAddressId));
    }
    
    @Test(dependsOnMethods = "addParams")
    public void addPatternTitles() throws Exception {
        titlePatternOrgPattern = "\"${param_" + paramOrgTitleId + "}\" ${param_" + paramOrgFormId + "}";
        titlePatternOrgId = ParamHelper.addPattern(Customer.OBJECT_TYPE, "Organization", titlePatternOrgPattern);
        // TODO: Pattern for person with birthday date.
    }

    @Test(dependsOnMethods = "addPatternTitles")
    public void addCustomers() throws Exception {
        var con = DbTest.conRoot;

        var paramDao = new ParamValueDAO(con);

        customerPersonIvan = CustomerHelper.addCustomer(0, paramGroupPersonId, CUSTOMER_PERS_IVAN_NAME);
        
        int customerId = customerPersonIvan.getId();
        paramDao.updateParamEmail(customerId, paramEmailId, 0, new ParameterEmailValue(CUSTOMER_PERS_IVAN_MAIL));
        paramDao.updateParamDate(customerId, paramBirthDateId, new GregorianCalendar(1983, Calendar.JULY, 1).getTime());
        paramDao.updateParamText(customerId, paramBirthPlaceId, "Uzgala village");
        paramDao.updateParamText(customerId, paramLivingAddressId, "Lenina Street 1, 450000, Ufa Russia");

        customerOrgNs = CustomerHelper.addCustomer(titlePatternOrgId, paramGroupOrgId, "");
        
        paramDao.updateParamEmail(customerOrgNs.getId(), paramEmailId, 0, new ParameterEmailValue(CUSTOMER_ORG_NS_TILL_MAIL, CUSTOMER_ORG_NS_TILL_NAME));
        paramDao.updateParamEmail(customerOrgNs.getId(), paramEmailId, 0, new ParameterEmailValue(CUSTOMER_ORG_NS_DOMAIN, "Domain"));
        paramDao.updateParamText(customerOrgNs.getId(), paramOrgTitleId, "NicroSoft");
        paramDao.updateParamList(customerOrgNs.getId(), paramOrgFormId, Collections.singleton(2));

        var customerDao = new CustomerDAO(con);
        customerOrgNs.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customerOrgNs.getId(), paramDao, titlePatternOrgPattern));
        customerDao.updateCustomer(customerOrgNs);
    }
}
