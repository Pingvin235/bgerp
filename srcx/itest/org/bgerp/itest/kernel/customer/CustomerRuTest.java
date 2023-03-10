package org.bgerp.itest.kernel.customer;

import static org.bgerp.itest.kernel.customer.CustomerTest.posParam;

import java.util.Collections;
import java.util.Set;

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
import ru.bgcrm.util.Utils;

@Test(groups = "customerRu", dependsOnGroups = { "param", "customer" })
public class CustomerRuTest {
    public static volatile int paramEmailId;
    public static volatile int paramPhoneId;

    private int paramBirthDateId;
    private int paramBirthPlaceId;
    public static volatile int paramAddressId;
    private int paramServiceAddressId;

    public static volatile int paramInnId;
    public static volatile int paramKppId;
    public static volatile int paramBankTitleId;
    public static volatile int paramBankBicId;
    public static volatile int paramBankCorrAccountId;
    public static volatile int paramBankAccountId;

    public static volatile int paramSignPostId;
    public static volatile int paramSignId;
    public static volatile int paramSignNameId;
    public static volatile int paramStampId;

    private int paramOrgTitleId;
    private int paramOrgFormId;

    private int titlePatternOrgId;
    private String titlePatternOrgPattern;

    private int paramGroupOrgId;
    private int paramGroupPersonId;

    public static volatile Customer customerOrgIvan;
    public static volatile Customer customerPersonIvan;

    @Test
    public void param() throws Exception {
        paramEmailId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_EMAIL, "Email(ы)", posParam += 2, ParamTest.MULTIPLE, "");
        paramPhoneId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_PHONE, "Телефон", posParam += 2 , "", "");

        // TODO: Make date chooser configuration.
        paramBirthDateId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_DATE, "Дата рождения", posParam += 2, "", "");
        paramBirthPlaceId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Место рождения", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Адрес", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramServiceAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_ADDRESS, "Адрес услуги", posParam += 2, ParamTest.MULTIPLE, "");

        paramOrgTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Наименование организации", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramOrgFormId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_LIST, "Форма собственности", posParam += 2, "",
                ResourceHelper.getResource(this, "orgforms.txt"));

        paramInnId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "ИНН", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramKppId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "КПП", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Банк", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankBicId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "БИК", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankCorrAccountId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Корр. счёт", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankAccountId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Расчётный счёт", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");

        paramSignPostId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Подпись должность", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramSignId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_FILE, "Подпись факсимиле", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramSignNameId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Подпись расшифровка", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramStampId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_FILE, "Печать факсимиле", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
    }

    @Test(dependsOnMethods = "param")
    public void paramGroup() throws Exception {
        paramGroupOrgId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Организация", Set.of(
                paramEmailId, paramPhoneId, paramOrgFormId, paramOrgTitleId, paramAddressId,
                paramInnId, paramKppId, paramBankTitleId, paramBankBicId, paramBankCorrAccountId, paramBankAccountId,
                paramServiceAddressId,
                paramSignPostId, paramSignId, paramSignNameId, paramStampId));
        paramGroupPersonId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Физ. лицо",
                Set.of(paramEmailId, paramPhoneId, paramBirthDateId, paramBirthPlaceId, paramAddressId, paramServiceAddressId));
    }

    @Test(dependsOnMethods = "param")
    public void patternTitle() throws Exception {
        titlePatternOrgPattern = "${param_" + paramOrgFormId + "} \"${param_" + paramOrgTitleId + "}\"";
        titlePatternOrgId = ParamHelper.addPattern(Customer.OBJECT_TYPE, "Organization", titlePatternOrgPattern);
        // TODO: Pattern for person with birthday date.
    }

    @Test(dependsOnMethods = "patternTitle")
    public void customer() throws Exception {
        var con = DbTest.conRoot;

        var paramDao = new ParamValueDAO(con);

        customerOrgIvan = CustomerHelper.addCustomer(titlePatternOrgId, paramGroupOrgId, "");

        int customerId = customerOrgIvan.getId();
        paramDao.updateParamText(customerId, paramOrgTitleId, "Образцов Иван Иванович");
        paramDao.updateParamList(customerId, paramOrgFormId, Collections.singleton(72));
        paramDao.updateParamText(customerId, paramAddressId, "450000, г. Уфа, ул. Карла Маркса, д. 12, корп. 1 Литера А, кв. 25");

        var customerDao = new CustomerDAO(con);
        customerOrgIvan.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customerId, paramDao, titlePatternOrgPattern));
        customerDao.updateCustomer(customerOrgIvan);

        paramDao.updateParamText(customerId, paramInnId, "123456789012");
        paramDao.updateParamText(customerId, paramKppId, "123456789");
        paramDao.updateParamText(customerId, paramBankTitleId, "ОБРАЗЦОВЫЙ БАНК г Уфа");
        paramDao.updateParamText(customerId, paramBankBicId, "123456789");
        paramDao.updateParamText(customerId, paramBankCorrAccountId, "30101810600000000957");
        paramDao.updateParamText(customerId, paramBankAccountId, "40817810099910004312");

        paramDao.updateParamText(customerId, paramSignPostId, "Индивидуальный предприниматель");
        paramDao.updateParamFile(customerId, paramSignId, 0, new FileData("sign.png", ResourceHelper.getResourceBytes(this, "sign.png")));
        paramDao.updateParamText(customerId, paramSignNameId, "И.И.Образцов");
        paramDao.updateParamFile(customerId, paramStampId, 0, new FileData("stamp.png", ResourceHelper.getResourceBytes(this, "stamp.png")));
    }
}
