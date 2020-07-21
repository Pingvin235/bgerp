package org.bgerp.itest.kernel.param;



import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;
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

    public static volatile int paramProcessNextDateId;
    public static volatile int paramProcessDeadlineDateId;
    
    private static final String MULTIPLE = "multiple=1";
    private static final String SAVE_ON_FOCUS_LOST = "saveOn=focusLost";
    
    @Test
    public void addConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Parameters", ResourceHelper.getResource(this, "config.txt"));
    }
    
    @Test
    public void addParamsCustomer() throws Exception {
        int pos = 0;
        paramCustomerEmailId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_EMAIL, "Email(s)", pos += 2, MULTIPLE, "");
        
        // TODO: Make date chooser configuration.
        paramCustomerBirthDateId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_DATE, "Birth date", pos += 2, "", "");
        paramCustomerBirthPlaceId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Birth place", pos += 2, SAVE_ON_FOCUS_LOST, "");
        paramCustomerLivingAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Residential address", pos += 2, SAVE_ON_FOCUS_LOST, "");
        
        paramCustomerOrgTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Organization title", pos += 2, SAVE_ON_FOCUS_LOST, "");
        paramCustomerOrgFormId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_LIST, "Organization form", pos += 2, "", ResourceHelper.getResource(this, "orgforms.txt"));
        
        // IBAN, BIC - with validation
        
        // Russian INN, KPP
    }
    
    @Test(dependsOnMethods = "addParamsCustomer")
    public void addCustomerParamGroup() throws Exception {
        ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Person",
                Sets.newHashSet(paramCustomerBirthDateId, paramCustomerBirthPlaceId, paramCustomerLivingAddressId));
        ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Organization", Sets.newHashSet(paramCustomerOrgFormId, paramCustomerOrgTitleId));
    }
    
    @Test(dependsOnMethods = "addParamsCustomer")
    public void addCustomerPatternTitle() throws Exception {
       titlePatternCustomerOrgId = ParamHelper.addPattern(Customer.OBJECT_TYPE, "Organization", "\"${param_" + paramCustomerOrgTitleId + "}\" ${param_" + paramCustomerOrgFormId + "}");
    }
    
    @Test
    public void addParamsUser() throws Exception {
        int pos = 0;
        paramUserEmailId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_EMAIL, "E-Mail(s)", pos += 2, MULTIPLE, "");
        paramUserCellPhoneId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_PHONE, "Cell phone(s)", pos += 2, "", "");
    }

    @Test
    public void addParamsProcess() throws Exception {
        int pos = 0;
        // TODO: Make date chooser configuration.
        paramProcessNextDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Next date", pos += 2, "", "");
        paramProcessDeadlineDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Deadline", pos += 2, "", "");
    }

}
