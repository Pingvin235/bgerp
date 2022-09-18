package org.bgerp.itest.kernel.message;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.CustomerHelper;
import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.customer.CustomerTest;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.bgerp.itest.kernel.user.UserTest;
import org.bgerp.model.Pageable;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.message.config.MessageRelatedProcessConfig;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressCountry;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessLink;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.util.Setup;

@Test(groups = "messageRelatedProcess", dependsOnGroups = { "message", "customer", "process", "address" })
public class MessageRelatedProcessTest {
    private static final String TITLE = MessageTest.TITLE + " Related Process";

    private static final String CUSTOMER_PHONE_NUMBER = "7347100500";

    private int paramCityId;

    private int processTypeId;
    // has message call from same number
    private int process1Id;
    // has same customer linked with phone number, as found
    private int process2Id;
    private Customer customer2;
    private CommonObjectLink process2Customer2Link;
    // related by city ID
    private int process3Id;
    private Customer customer3;
    private AddressCity city3;
    private AddressHouse house3;

    @Test
    public void param() throws Exception {
        paramCityId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_LIST, TITLE + " City", ProcessTest.posParam += 2,
                "multiple=1\ndirectory=address_city\n", "");
    }

    @Test(dependsOnMethods = "param")
    public void config() throws Exception {
        var config =
                ConfigHelper.generateConstants(
                    "PROCESS_CITY_PARAM_ID", paramCityId
                ) +
                ResourceHelper.getResource(this, "config.txt");
        ConfigHelper.addIncludedConfig(TITLE, config);

        var messageRelatedProcessConfig = Setup.getSetup().getConfig(MessageRelatedProcessConfig.class);
        Assert.assertNotNull(messageRelatedProcessConfig);
        Assert.assertEquals(messageRelatedProcessConfig.getTypes().size(), 3);
        Assert.assertEquals(messageRelatedProcessConfig.getTypes().get(1), MessageRelatedProcessConfig.Type.MESSAGE_FROM);
        Assert.assertEquals(messageRelatedProcessConfig.getTypes().get(2), MessageRelatedProcessConfig.Type.FOUND_LINK);
        Assert.assertEquals(messageRelatedProcessConfig.getTypes().get(3), MessageRelatedProcessConfig.Type.FOUND_LINK_CUSTOMER_ADDRESS_CITY);
    }

    @Test(dependsOnMethods = "param")
    public void processType() throws Exception {
        var props = new TypeProperties();
        props.setStatusIds(List.of(ProcessTest.statusOpenId, ProcessTest.statusDoneId));
        props.setCreateStatus(ProcessTest.statusOpenId);
        props.setCloseStatusIds(Set.of(ProcessTest.statusDoneId));
        props.getParameterIds().addAll(List.of(paramCityId));

        processTypeId = ProcessHelper.addType(TITLE, ProcessTest.processTypeTestGroupId, false, props).getId();
    }

    @Test
    public void address() throws Exception {
        var dao = new AddressDAO(DbTest.conRoot);

        var country = dao.updateAddressCountry(new AddressCountry().withTitle(TITLE + " Country"));
        city3 = dao.updateAddressCity(new AddressCity().withCountryId(country.getId()).withTitle(TITLE + "City"));
        var street = dao.updateAddressStreet(new AddressItem().withCityId(city3.getId()).withTitle(TITLE + " Street"));
        house3 = dao.updateAddressHouse(new AddressHouse().withStreetId(street.getId()).withPostIndex("42").withHouseAndFrac("42"));
    }

    @Test(dependsOnMethods = "address")
    public void customer() throws Exception {
        var paramDao = new ParamValueDAO(DbTest.conRoot);

        customer2 = CustomerHelper.addCustomer(0, CustomerTest.paramGroupPersonId, TITLE + " Customer 2");
        paramDao.updateParamPhone(customer2.getId(), CustomerTest.paramPhoneId,
                new ParameterPhoneValue(List.of(new ParameterPhoneValueItem(CUSTOMER_PHONE_NUMBER, "13", ""))));

        customer3 = CustomerHelper.addCustomer(0, CustomerTest.paramGroupPersonId, TITLE + " Customer 3");
        paramDao.updateParamPhone(customer3.getId(), CustomerTest.paramPhoneId,
                new ParameterPhoneValue(List.of(new ParameterPhoneValueItem(CUSTOMER_PHONE_NUMBER, "13", ""))));
        paramDao.updateParamAddress(customer3.getId(), CustomerTest.paramServiceAddressId, 0,
                new ParameterAddressValue().withHouseId(house3.getId()).withFlat("42"));
    }

    @Test(dependsOnMethods = { "processType", "customer" })
    public void process() throws Exception {
        var paramDao = new ParamValueDAO(DbTest.conRoot);
        var linkDao = new ProcessLinkDAO(DbTest.conRoot);

        process1Id = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " 1").getId();

        process2Id = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " 2").getId();
        process2Customer2Link = new ProcessLink(process2Id, Customer.OBJECT_TYPE, customer2.getId(), customer2.getTitle());
        linkDao.addLink(process2Customer2Link);

        process3Id = ProcessHelper.addProcess(processTypeId, UserTest.USER_ADMIN_ID, TITLE + " 3").getId();
        paramDao.updateParamList(process3Id, paramCityId, Set.of(city3.getId()));
    }

    @Test(dependsOnMethods = { "process", "config" })
    public void message() throws Exception {
        // unprocessed message, used to search related processes
        MessageHelper.addCallMessage(0, UserTest.USER_ADMIN_ID, Duration.ZERO, CUSTOMER_PHONE_NUMBER, "100",
                TITLE + " Unprocessed message", "");

        // message with same from address
        MessageHelper.addCallMessage(process1Id, UserTest.USER_ADMIN_ID, Duration.ZERO, CUSTOMER_PHONE_NUMBER, "100",
                TITLE + " Process 1 Message", "");

        var dao = new ProcessDAO(DbTest.conRoot);

        var pageable = new Pageable<Pair<Process, MessageRelatedProcessConfig.Type>>();
        dao.searchProcessListForMessage(pageable, CUSTOMER_PHONE_NUMBER,
                List.of(process2Customer2Link, new ProcessLink(0, Customer.OBJECT_TYPE, customer3.getId(), "")), true);

        var list = pageable.getList();
        Assert.assertEquals(list.size(), 3);
    }
}
