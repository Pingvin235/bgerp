package org.bgerp.itest.kernel.customer;

import static org.bgerp.itest.kernel.customer.CustomerTest.posParam;

import java.util.Collections;
import java.util.Set;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.itest.helper.CustomerHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.bgerp.model.file.FileData;
import org.bgerp.model.param.Parameter;
import org.testng.annotations.Test;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.util.Utils;

@Test(groups = "customerRu", dependsOnGroups = { "param", "customer" })
public class CustomerRuTest {
    // физ. лицо.
    private int paramBirthDateId;
    private int paramBirthPlaceId;
    private int paramRegAddressId;

    // организация
    private int paramOrgTitleId;
    private int paramOrgFormId;

    public static volatile int paramJurAddressId;
    public static volatile int paramPostAddressId;
    public static volatile int paramInnId;
    public static volatile int paramKppId;
    // 13 юр. лица, 15 - ИП
    public static volatile int paramOgrnId;
    public static volatile int paramBankTitleId;
    public static volatile int paramBankBicId;
    public static volatile int paramBankCorrAccountId;
    public static volatile int paramBankAccountId;

    public static volatile int paramSignPostId;
    public static volatile int paramSignId;
    public static volatile int paramSignNameId;
    public static volatile int paramStampId;
    public static volatile int paramInvoiceFooterId;

    // общие
    public static volatile int paramEmailId;
    public static volatile int paramPhoneId;
    private int paramServiceAddressId;

    private int titlePatternOrgId;
    private String titlePatternOrgPattern;

    //
    private int paramGroupOrgId;
    private int paramGroupPersonId;

    public static volatile Customer customerOrgIvan;
    public static volatile Customer customerPersonIvan;

    @Test
    public void param() throws Exception {
        // TODO: Make date chooser configuration.
        paramBirthDateId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_DATE, "Дата рождения", posParam += 2, "", "");
        paramBirthPlaceId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Место рождения", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramRegAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Адрес регистрации", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");

        paramOrgTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Наименование организации", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramOrgFormId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_LIST, "Форма собственности", posParam += 2, "",
                ResourceHelper.getResource(this, "orgforms.txt"));

        paramInnId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "ИНН", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramKppId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "КПП", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramJurAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Адрес юридический", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramPostAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Адрес почтовый (если отличается)", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramOgrnId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "ОГРН(ИП)", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankTitleId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Банк", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankBicId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "БИК", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankCorrAccountId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Корр. счёт", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramBankAccountId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Расчётный счёт", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");

        paramSignPostId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Подпись должность", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramSignId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_FILE, "Подпись факсимиле", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramSignNameId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Подпись расшифровка", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramStampId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_FILE, "Печать факсимиле", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");
        paramInvoiceFooterId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_TEXT, "Текст под суммой счёта", posParam += 2, ParamTest.SAVE_ON_FOCUS_LOST, "");

        paramEmailId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_EMAIL, "Email(ы)", posParam += 2, ParamTest.MULTIPLE, "");
        paramPhoneId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_PHONE, "Телефон", posParam += 2 , "", "");
        paramServiceAddressId = ParamHelper.addParam(Customer.OBJECT_TYPE, Parameter.TYPE_ADDRESS, "Адрес(а) услуги", posParam += 2, ParamTest.MULTIPLE, "");
    }

    @Test(dependsOnMethods = "param")
    public void paramGroup() throws Exception {
        paramGroupOrgId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Организация", Set.of(
                paramOrgFormId, paramOrgTitleId,
                paramJurAddressId, paramPostAddressId,
                paramInnId, paramKppId, paramOgrnId,
                paramBankTitleId, paramBankBicId, paramBankCorrAccountId, paramBankAccountId,
                paramSignPostId, paramSignId, paramSignNameId, paramStampId,
                paramInvoiceFooterId,
                paramEmailId, paramPhoneId, paramServiceAddressId));
        paramGroupPersonId = ParamHelper.addParamGroup(Customer.OBJECT_TYPE, "Физ. лицо", Set.of(
                paramBirthDateId, paramBirthPlaceId,
                paramRegAddressId,
                paramEmailId, paramPhoneId, paramServiceAddressId));
    }

    @Test(dependsOnMethods = "param")
    public void patternTitle() throws Exception {
        titlePatternOrgPattern = "${param_" + paramOrgFormId + "} \"${param_" + paramOrgTitleId + "}\"";
        titlePatternOrgId = ParamHelper.addPattern(Customer.OBJECT_TYPE, "Организация", titlePatternOrgPattern);
        // TODO: Pattern for person with birthday date.
    }

    @Test(dependsOnMethods = "patternTitle")
    public void customerOrg() throws Exception {
        var con = DbTest.conRoot;

        var paramDao = new ParamValueDAO(con);

        customerOrgIvan = CustomerHelper.addCustomer(titlePatternOrgId, paramGroupOrgId, "");

        int customerId = customerOrgIvan.getId();
        paramDao.updateParamText(customerId, paramOrgTitleId, "Образцов Иван Иванович");
        paramDao.updateParamList(customerId, paramOrgFormId, Collections.singleton(72));
        paramDao.updateParamText(customerId, paramJurAddressId, "450000, г. Уфа, ул. Карла Маркса, д. 12, корп. 1 Литера А, кв. 25");

        var customerDao = new CustomerDAO(con);
        customerOrgIvan.setTitle(Utils.formatPatternString(Customer.OBJECT_TYPE, customerId, paramDao, titlePatternOrgPattern));
        customerDao.updateCustomer(customerOrgIvan);

        paramDao.updateParamText(customerId, paramInnId, "123456789012");
        paramDao.updateParamText(customerId, paramKppId, "123456789");
        paramDao.updateParamText(customerId, paramOgrnId, "1234567890123");
        paramDao.updateParamText(customerId, paramBankTitleId, "ОБРАЗЦОВЫЙ БАНК г Уфа");
        paramDao.updateParamText(customerId, paramBankBicId, "123456789");
        paramDao.updateParamText(customerId, paramBankCorrAccountId, "30101810600000000957");
        paramDao.updateParamText(customerId, paramBankAccountId, "40817810099910004312");

        paramDao.updateParamText(customerId, paramSignPostId, "Индивидуальный предприниматель");
        paramDao.updateParamFile(customerId, paramSignId, 0, new FileData("sign.png", ResourceHelper.getResourceBytes(this, "sign.png")));
        paramDao.updateParamText(customerId, paramSignNameId, "И.И.Образцов");
        paramDao.updateParamFile(customerId, paramStampId, 0, new FileData("stamp.png", ResourceHelper.getResourceBytes(this, "stamp.png")));
        paramDao.updateParamText(customerId, paramInvoiceFooterId, "На основании Договора-оферты от 17.02.2022, расположенному по адресу: https://bgerp.ru/client/");

        // TODO: Email and phones.
    }

    @Test(dependsOnMethods = "patternTitle")
    public void customerPers() throws Exception {
        // TODO: Create customer and params.
    }
}
