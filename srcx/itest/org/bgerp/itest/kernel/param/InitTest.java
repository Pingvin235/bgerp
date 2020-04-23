package org.bgerp.itest.kernel.param;

import static org.bgerp.itest.kernel.db.DbTest.conPoolRoot;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import ru.bgcrm.dao.ParamDAO;
import ru.bgcrm.dao.ParamGroupDAO;
import ru.bgcrm.dao.PatternDAO;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.user.User;

@Test(groups = "paramInit", dependsOnGroups = "configInit")
public class InitTest {
    public static volatile int paramCustomerEmailId;
    
    public static volatile int paramCustomerBirthDateId;
    public static volatile int paramCustomerBirthPlaceId;
    public static volatile int paramCustomerLivingAddressId;
    
    public static volatile int paramCustomerOrgTitleId;
    public static volatile int paramCustomerOrgFormId;
    
    public static volatile int titlePatternCustomerOrgId;
    
    public static volatile int paramUserPhoneId;
    public static volatile int paramUserCellPhoneId;
    public static volatile int paramUserEmailId;
    public static volatile int paramUserTelegramId;
    public static volatile int paramUserWebSiteId;
    
    private static final String MULTIPLE = "multiple=1";
    private static final String SAVE_ON_FOCUS_LOST = "saveOn=focusLost";
    
    @Test
    public void initConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Parameters", ResourceHelper.getResource(this, "config.txt"));
    }
    
    @Test
    public void addParamsCustomer() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ParamDAO(con);
            
            int pos = 0;
            paramCustomerEmailId = ParamHelper.addParam(dao, Customer.OBJECT_TYPE, Parameter.TYPE_EMAIL, "Email(s)", pos += 2, MULTIPLE, "");
            
            paramCustomerBirthDateId = ParamHelper.addParam(dao, Customer.OBJECT_TYPE, Parameter.TYPE_DATE, "Birth date", pos += 2, "", "");
            paramCustomerBirthPlaceId = ParamHelper.addParam(dao, Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Birth place", pos += 2, SAVE_ON_FOCUS_LOST, "");
            paramCustomerLivingAddressId = ParamHelper.addParam(dao, Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Residential address", pos += 2, SAVE_ON_FOCUS_LOST, "");
            
            paramCustomerOrgTitleId = ParamHelper.addParam(dao, Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Organization title", pos += 2, SAVE_ON_FOCUS_LOST, "");
            paramCustomerOrgFormId = ParamHelper.addParam(dao, Customer.OBJECT_TYPE, Parameter.TYPE_LIST, "Organization form", pos += 2, "", ResourceHelper.getResource(this, "orgforms.txt"));
            
            // IBAN, BIC - with validation
            
            // Russian INN, KPP
            
            con.commit();
        }
    }
    
    @Test(dependsOnMethods = "addParamsCustomer")
    public void addCustomerParamGroup() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ParamGroupDAO(con);
            
            ParamHelper.addParamGroup(dao, Customer.OBJECT_TYPE, "Person", Sets.newHashSet(
                    paramCustomerBirthDateId, paramCustomerBirthPlaceId, paramCustomerLivingAddressId));
            ParamHelper.addParamGroup(dao, Customer.OBJECT_TYPE, "Organization", Sets.newHashSet(
                    paramCustomerOrgFormId, paramCustomerOrgTitleId));
            
            con.commit();
        }
    }
    
    @Test(dependsOnMethods = "addParamsCustomer")
    public void addCustomerPatternTitle() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var dao = new PatternDAO(con);
            
            titlePatternCustomerOrgId = ParamHelper.addPattern(dao, Customer.OBJECT_TYPE, "Organization", "\"${param_" + paramCustomerOrgTitleId + "}\" ${param_" + paramCustomerOrgFormId + "}");
            
            con.commit();
        }
    }
    
    @Test
    public void addParamsUser() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ParamDAO(con);
            
            int pos = 0;
            paramUserEmailId = ParamHelper.addParam(dao, User.OBJECT_TYPE, Parameter.TYPE_EMAIL, "E-Mail(s)", pos += 2, MULTIPLE, "");
            paramUserCellPhoneId = ParamHelper.addParam(dao, User.OBJECT_TYPE, Parameter.TYPE_PHONE, "Cell phone(s)", pos += 2, "", "");
            
            
            con.commit();
        }
    }

}
