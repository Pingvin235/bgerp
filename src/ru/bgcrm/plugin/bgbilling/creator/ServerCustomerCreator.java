package ru.bgcrm.plugin.bgbilling.creator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;
import org.bgerp.util.sql.LikePattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.ParamList;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.param.ParameterPhoneValueItem;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.TransferData;
import ru.bgcrm.plugin.bgbilling.creator.Config.ParameterGroupTitlePatternRule;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.LevenshteinDistance;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.SingleConnectionSet;

/**
 * Создатель контрагентов из одной базы.
 */
public class ServerCustomerCreator {
    private static final Log log = Log.getLog();

    private Config config;

    private final DBInfo dbInfo;
    private final TransferData transferData;

    private final User user;
    private final int customerIdParam;
    private final int pageSize;
    private final int minCustomerTitleLength;

    private final String billingId;

    // соответствие параметра контрагента параметр(ам) биллинга
    // несколько параметров биллинга поддержаны для адреса
    private Map<Parameter, ParamMappingValue> paramTypeMapping = new HashMap<Parameter, ParamMappingValue>();

    private Connection con;
    private CustomerDAO customerDao;
    private ParamValueDAO paramValueDao;
    private CustomerLinkDAO linkDao;

    private static class ParamMappingValue {
        private List<Integer> billingParamIdList;
        private List<Integer> crmListValues = Collections.emptyList();
        private List<Integer> billingListValues = Collections.emptyList();
    }

    public ServerCustomerCreator(Config config, ConfigMap params) throws BGException {
        this.config = config;

        billingId = params.get("billingId");
        dbInfo = DBInfoManager.getInstance().getDbInfoMap().get(billingId);
        if (dbInfo == null) {
            throw new BGException("Billing not defined for creator.");
        }

        String userName = params.get("user", "");
        String userPassword = params.get("pswd", "");
        if (Utils.isBlankString(userName) || Utils.isBlankString(userPassword)) {
            throw new BGException("Billing user or password undefined.");
        }

        user = new User();
        user.setLogin(userName);
        user.setPassword(userPassword);

        log.info("Creating server creator for: " + dbInfo.getId());

        try {
            transferData = dbInfo.getTransferData();
        } catch (Exception e) {
            throw new BGException(e.getMessage(), e);
        }

        for (String token : params.get("paramMapping", "").split(";")) {
            String[] pair = token.split(":");
            if (pair.length != 2) {
                throw new BGException("Incorrect param map: " + token);
            }

            String crmParam = pair[0].trim();
            String billingParam = pair[1].trim();

            ParamMappingValue value = new ParamMappingValue();

            String crmListValues = StringUtils.substringBetween(crmParam, "[", "]");
            if (Utils.notBlankString(crmListValues)) {
                crmParam = StringUtils.substringBefore(crmParam, "[");
                value.crmListValues = Utils.toIntegerList(crmListValues);
            }

            String billingListValues = StringUtils.substringBetween(billingParam, "[", "]");
            if (Utils.notBlankString(billingListValues)) {
                billingParam = StringUtils.substringBefore(billingParam, "[");
                value.billingListValues = Utils.toIntegerList(billingListValues);
            }

            Parameter param = ParameterCache.getParameter(Utils.parseInt(crmParam));
            if (param == null) {
                throw new BGException("Can't find param: " + crmParam);
            }

            value.billingParamIdList = Utils.toIntegerList(billingParam);

            log.info("Param mapping: " + param.getId() + " => " + value.billingParamIdList);
            paramTypeMapping.put(param, value);
        }

        customerIdParam = dbInfo.getCustomerIdParam();
        if (customerIdParam <= 0) {
            throw new BGException("customerIdParam not defined for billing server!");
        }

        pageSize = params.getInt("pageSize", 50);
        minCustomerTitleLength = params.getInt("minCustomerTitleLength", 10);
    }

    public String getBillingId() {
        return billingId;
    }

    private void init(Connection con) {
        this.con = con;
        customerDao = new CustomerDAO(con);
        paramValueDao = new ParamValueDAO(con);
        linkDao = new CustomerLinkDAO(con);
    }

    public void createCustomer(String billingId, Connection con, int contractId, int customerId) throws BGException {
        try {
            init(con);

            Request req = new Request();
            req.setModule("contract");
            req.setAction("FindContractByID");
            req.setAttribute("id", contractId);

            Document doc = transferData.postData(req, user);

            for (Element contract : XMLUtils.selectElements(doc, "/data/contracts/item")) {
                String title = contract.getAttribute("title");
                createCustomer(contractId, title);
            }

            SQLUtils.commitConnection(con);
        } catch (SQLException e) {
            throw new BGException(e);
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    public void createCustomers(Connection con) throws BGException {
        init(con);

        Request req = new Request();
        req.setModule("contract");
        req.setAction("FindContract");
        req.setAttribute("type", 1);
        req.setAttribute("parameters", customerIdParam);
        req.setAttribute("parameter", "^0$");
        req.setPageIndex(1);
        req.setPageSize(pageSize);

        log.info("Import customers for server: " + dbInfo.getId());

        try {
            Document doc = transferData.postData(req, user);

            for (Element contract : XMLUtils.selectElements(doc, "/data/contracts/item")) {
                int contractId = Utils.parseInt(contract.getAttribute("id"));
                String title = contract.getAttribute("title");

                createCustomer(contractId, title);
            }
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    public void createCustomer(int contractId, String title) throws Exception {
        int pos = title.indexOf('[');
        String contractNumber = title.substring(0, pos).trim();
        String customerTitle = title.substring(pos + 1, title.lastIndexOf(']')).trim();

        log.info("FOUND CONTRACT: " + contractNumber + "; id:" + contractId + "; customerTitle: " + customerTitle);

        if (customerTitle.length() < minCustomerTitleLength) {
            log.warn("Customer title length less when: " + minCustomerTitleLength);
            ContractParamDAO contractParamDAO = new ContractParamDAO(user, dbInfo);
            contractParamDAO.updateTextParameter(contractId, dbInfo.getCustomerIdParam(), "");
            return;
        }

        Pageable<Customer> result = new Pageable<Customer>();
        customerDao.searchCustomerList(result, LikePattern.SUB.get(customerTitle));

        // строковые представления параметров договоров
        Map<Integer, String> paramValues = getContractParamValues(contractId);

        Customer customer = findCustomerByTitleWithParamsConfirm(customerTitle, contractId, paramValues);

        if (customer != null) {
            log.info("Found customer by title and confirm: " + customer.getId());
        }

        if (customer == null) {
            customer = findCustomerByParamsWithTitleConfirm(customerTitle, contractId, paramValues);
            // создание нового контрагента, если не был найден подходящий
            if (customer == null) {
                customer = new Customer();
                customer.setTitle(customerTitle);
                customer.setTitlePattern("");

                ParameterGroupTitlePatternRule rule = config.getCustomerParameterGroup(contractNumber);
                if (rule != null) {
                    customer.setParamGroupId(rule.parameterGroupId);
                    customer.setTitlePatternId(rule.titlePatternId);
                }
                customerDao.updateCustomer(customer);

                log.info("Created new customer. ID: " + customer.getId() + "; parameterGroupId: " + customer.getParamGroupId());
            } else {
                customer.setTitle(customer.getTitle() + " | " + customerTitle);
                customerDao.updateCustomer(customer);

                log.info("Found customer by param and title: " + customer.getId());
            }
        }

        linkCustomer(customer, contractId, contractNumber, paramValues);

        SQLUtils.commitConnection(con);
    }

    private Map<Integer, String> getContractParamValues(int contractId) throws Exception {
        log.info("Load contract parameters.");

        Map<Integer, String> result = new HashMap<Integer, String>();

        ContractParamDAO contractParamDAO = new ContractParamDAO(user, dbInfo);
        Document doc = contractParamDAO.getContractParams(contractId);

        for (Element param : XMLUtils.selectElements(doc, "/data/parameters/parameter")) {
            int type = Utils.parseInt(param.getAttribute("pt"));
            int billingParamId = Utils.parseInt(param.getAttribute("pid"));
            String value = param.getAttribute("value").trim();

            if (Utils.isBlankString(value)) {
                continue;
            }

            if (type == 9) {
                value = value.replaceAll("[^\\d;]", "");
            }

            result.put(billingParamId, value);

            log.info(billingParamId + " => " + value);
        }

        return result;
    }

    // поиск контрагента по имени с подтверждением по параметрам
    private Customer findCustomerByTitleWithParamsConfirm(String customerTitle, int contractId, Map<Integer, String> paramValues) throws Exception {
        Pageable<Customer> result = new Pageable<Customer>();
        result.getPage().setPageSize(300);
        result.getPage().setPageIndex(1);

        customerDao.searchCustomerList(result, LikePattern.SUB.get(customerTitle));

        // есть уже контрагенты с таким наименованием
        if (result.getList().size() != 0) {
            for (Customer customer : result.getList()) {
                // сверка по ключевым параметрам
                for (Parameter param : config.confirmParameterList) {
                    ParamMappingValue mapping = paramTypeMapping.get(param);
                    if (mapping == null) {
                        continue;
                    }

                    // поочерёдно выбираем значение параметра из нескольких значений (может быть старое значение параметра)
                    String billingParamValue = null;
                    for (Integer paramId : mapping.billingParamIdList) {
                        billingParamValue = paramValues.get(paramId);
                        if (billingParamValue != null) {
                            break;
                        }
                    }

                    if (billingParamValue == null) {
                        continue;
                    }

                    // сверка значений с параметром в контрагенте

                    if (Parameter.TYPE_TEXT.equals(param.getType())) {
                        String val = paramValueDao.getParamText(customer.getId(), param.getId());
                        if (val != null && val.trim().equals(billingParamValue)) {
                            log.info("Confirm param, text: " + param.getId());

                            return customer;
                        }
                    } else if (Parameter.TYPE_ADDRESS.equals(param.getType())) {
                        for (ParameterAddressValue addr : paramValueDao.getParamAddress(customer.getId(), param.getId()).values()) {
                            if (addr.getValue().trim().equals(billingParamValue)) {
                                log.info("Confirm param, address: " + param.getId());

                                return customer;
                            }
                        }
                    } else if (Parameter.TYPE_PHONE.equals(param.getType())) {
                        Set<String> contractPhones = new HashSet<String>(Arrays.asList(billingParamValue.split(";")));

                        ParameterPhoneValue val = paramValueDao.getParamPhone(customer.getId(), param.getId());
                        if (val != null) {
                            for (ParameterPhoneValueItem item : val.getItemList()) {
                                if (contractPhones.contains(item.getPhone().trim())) {
                                    log.info("Confirm param, phone: " + param.getId());
                                    return customer;
                                }
                            }
                        }
                    } else if (Parameter.TYPE_DATE.equals(param.getType())) {
                        String val = TimeUtils.format(paramValueDao.getParamDate(customer.getId(), param.getId()), TimeUtils.FORMAT_TYPE_YMD);
                        if (val != null && val.trim().equals(billingParamValue)) {
                            log.info("Confirm param, date: " + param.getId());

                            return customer;
                        }
                    }
                }
            }
        }

        return null;
    }

    // поиск контрагента по ключевым параметрам с подтверждением по совпадению имени
    private Customer findCustomerByParamsWithTitleConfirm(String customerTitle, int contractId, Map<Integer, String> paramValues) throws Exception {
        // поиск по ключевым параметрам
        for (Parameter param : config.confirmParameterList) {
            ParamMappingValue mapping = paramTypeMapping.get(param);
            if (mapping == null) {
                continue;
            }

            for (int billingParamId : mapping.billingParamIdList) {
                String stringValue = paramValues.get(billingParamId);
                if (Utils.isBlankString(stringValue)) {
                    continue;
                }

                if (Parameter.TYPE_ADDRESS.equals(param.getType())) {
                    ContractParamDAO contractParamDAO = new ContractParamDAO(user, dbInfo);
                    ParamAddressValue paramAddressValue = contractParamDAO.getAddressParam(contractId, billingParamId);
                    ParameterAddressValue billingAddr = ContractParamDAO.toCrmObject(paramAddressValue, con);

                    Pageable<ParameterSearchedObject<Customer>> searchResult = new Pageable<ParameterSearchedObject<Customer>>();
                    customerDao.searchCustomerListByAddress(searchResult, Collections.singletonList(param.getId()), billingAddr.getHouseId(),
                            billingAddr.getFlat(), billingAddr.getRoom());

                    for (ParameterSearchedObject<Customer> result : searchResult.getList()) {
                        Customer customer = result.getObject();
                        String title = customer.getTitle();

                        int titleDistance = config.getMaxTitleDistance(param.getId());
                        if (titleDistance < 0 || LevenshteinDistance.computeLevenshteinDistance(title, customerTitle) <= titleDistance) {
                            return customer;
                        }
                    }
                } else if (Parameter.TYPE_PHONE.equals(param.getType())) {
                    String[] phones = stringValue.split(";");

                    Pageable<Customer> searchResult = new Pageable<Customer>();
                    customerDao.searchCustomerListByPhone(searchResult, Collections.singletonList(param.getId()), phones);

                    Customer customer = searchCustomer(searchResult, param.getId(), customerTitle);
                    if (customer != null) {
                        return customer;
                    }

                } else if (Parameter.TYPE_TEXT.equals(param.getType())) {
                    Pageable<Customer> searchResult = new Pageable<Customer>();
                    customerDao.searchCustomerListByText(searchResult, Collections.singletonList(param.getId()), LikePattern.SUB.get(stringValue));

                    Customer customer = searchCustomer(searchResult, param.getId(), customerTitle);
                    if (customer != null) {
                        return customer;
                    }
                }
            }
        }

        return null;
    }

    private Customer searchCustomer(Pageable<Customer> searchResult, int paramId, String customerTitle) {
        int titleDistance = config.getMaxTitleDistance(paramId);
        for (Customer customer : searchResult.getList()) {
            String title = customer.getTitle();
            if (titleDistance < 0 || LevenshteinDistance.computeLevenshteinDistance(title, customerTitle) <= titleDistance) {
                return customer;
            }
        }
        return null;
    }

    // привязка договора к контрагенту, перенос параметров
    private void linkCustomer(Customer customer, int contractId, String contractNumber, Map<Integer, String> paramValues) throws Exception {
        log.info("Linking contract: " + contractId + " to customer: " + customer.getId());

        // привязка договора к контрагенту
        CommonObjectLink link = new CommonObjectLink(Customer.OBJECT_TYPE, customer.getId(), "contract:" + dbInfo.getId(), contractId,
                contractNumber);

        linkDao.deleteLinksTo(link);

        // для привязки в биллинге
        EventProcessor.processEvent(new LinkAddingEvent(new DynActionForm(user), link), new SingleConnectionSet(con));

        linkDao.addLink(link);

        // указание в биллинге кода контрагента
        //contractDao.updateParamText( contractId, customerIdParam, String.valueOf( customer.getId() ) );

        for (Parameter param : config.importParameterList) {
            ParamMappingValue mapping = paramTypeMapping.get(param);
            if (mapping == null) {
                log.info("Not found billing params for import customer param: " + param.getId());
                continue;
            }

            for (int billingParamId : mapping.billingParamIdList) {
                // проверка, что параметр договора заполнен
                String stringValue = paramValues.get(billingParamId);
                if (Utils.isBlankString(stringValue)) {
                    continue;
                }

                log.info("Checking import parameter " + param.getId() + " with billing value " + stringValue);

                if (Parameter.TYPE_TEXT.equals(param.getType())) {
                    paramValueDao.updateParamText(customer.getId(), param.getId(), stringValue);

                    log.info("Update text param: " + param.getId() + ", value: " + stringValue);
                } else if (Parameter.TYPE_DATE.equals(param.getType())) {
                    Date date = TimeUtils.parse(stringValue, TimeUtils.FORMAT_TYPE_YMD);
                    if (date != null) {
                        paramValueDao.updateParamDate(customer.getId(), param.getId(), date);

                        log.info("Update date param: " + param.getId() + ", value: " + stringValue);
                    } else {
                        log.error("Incorrect date param: " + stringValue);
                    }
                } else if (Parameter.TYPE_PHONE.equals(param.getType())) {
                    Set<String> billingValues = new HashSet<String>(Arrays.asList(stringValue.split(";")));

                    ParameterPhoneValue customerParamValue = paramValueDao.getParamPhone(customer.getId(), param.getId());
                    if (customerParamValue != null) {
                        // удаление из номеров параметра договора всего что уже есть в контрагенте
                        for (ParameterPhoneValueItem item : customerParamValue.getItemList()) {
                            billingValues.remove(item.getPhone());
                        }
                    } else {
                        customerParamValue = new ParameterPhoneValue();
                        customerParamValue.setItemList(new ArrayList<ParameterPhoneValueItem>());
                    }

                    // если остались номера которых нет
                    if (billingValues.size() > 0) {
                        log.info("Add phone param values, param: " + param.getId() + "; values: " + Utils.toString(billingValues));

                        ContractParamDAO contractParamDAO = new ContractParamDAO(user, dbInfo);
                        List<ParameterPhoneValueItem> paramPhoneValueItemList = contractParamDAO.getPhoneParam(contractId, billingParamId);
                        ParameterPhoneValue parameterPhoneValue = new ParameterPhoneValue(paramPhoneValueItemList);

                        for (ParameterPhoneValueItem item : parameterPhoneValue.getItemList()) {
                            if (!billingValues.contains(item.getPhone())) {
                                continue;
                            }

                            log.info("Value: " + item.getPhone() + "; format: " + item.getFormat());

                            customerParamValue.getItemList().add(item);
                        }

                        paramValueDao.updateParamPhone(customer.getId(), param.getId(), customerParamValue);
                    }
                } else if (Parameter.TYPE_ADDRESS.equals(param.getType())) {
                    ContractParamDAO contractParamDAO = new ContractParamDAO(user, dbInfo);
                    ParamAddressValue paramAddressValue = contractParamDAO.getAddressParam(contractId, billingParamId);
                    ParameterAddressValue billingAddr = ContractParamDAO.toCrmObject(paramAddressValue, con);

                    boolean addressExist = false;

                    for (ParameterAddressValue value : paramValueDao.getParamAddress(customer.getId(), param.getId()).values()) {
                        addressExist = value.getHouseId() == billingAddr.getHouseId() && value.getFlat().equals(billingAddr.getFlat())
                                && value.getRoom().equals(billingAddr.getRoom());
                        if (addressExist) {
                            break;
                        }
                    }

                    if (!addressExist) {
                        billingAddr.setValue(AddressUtils.buildAddressValue(billingAddr, con));
                        paramValueDao.updateParamAddress(customer.getId(), param.getId(), 0, billingAddr);

                        log.info("Add address param value, param: " + param.getId() + "; value: " + billingAddr.getValue());
                    }
                } else if (Parameter.TYPE_LIST.equals(param.getType())) {
                    ContractParamDAO contractParamDAO = new ContractParamDAO(user, dbInfo);
                    ParamList billingValue = contractParamDAO.getListParamValue(contractId, billingParamId);

                    if (billingValue == null) {
                        continue;
                    }

                    // маппинг
                    if (mapping.billingListValues.size() > 0) {
                        int pos = mapping.billingListValues.indexOf(billingValue.getId());
                        if (pos < 0) {
                            log.error("Not found billing param list value: " + billingValue.getId());
                            continue;
                        }

                        if (pos >= mapping.crmListValues.size()) {
                            log.error("Not found crm param list value for billing value: " + billingValue.getId());
                            continue;
                        }

                        int crmListParamValue = mapping.crmListValues.get(pos);

                        // добавление значения на случай установки противоречивых значений будет хоть видно
                        Set<Integer> values = paramValueDao.getParamList(customer.getId(), param.getId());
                        values.add(crmListParamValue);

                        paramValueDao.updateParamList(customer.getId(), param.getId(), values);

                        log.info("Add list param value, param: " + param.getId() + "; value: " + crmListParamValue);
                    } else {
                        paramValueDao.updateParamList(customer.getId(), param.getId(), Collections.singleton(billingValue.getId()));

                        log.info("Add list param value, param: " + param.getId() + "; value: " + billingValue.getId());
                    }
                } else if (Parameter.TYPE_EMAIL.equals(param.getType())) {
                    Map<String, String> billingValues = new HashMap<String, String>();
                    for (String value : Arrays.asList(stringValue.split("[;,]"))) {
                        value = value.trim();
                        if (Utils.isEmptyString(value)) {
                            continue;
                        }

                        String email = "";
                        String comment = "";

                        // в 5.2 возможен вариант EMail ов по RFC: Иванов Иван Иванович <ivan@ivan.com>
                        try {
                            InternetAddress addr = InternetAddress.parse(value)[0];
                            email = addr.getAddress();
                            comment = Utils.maskNull(addr.getPersonal());
                        }
                        // вариант для строки Email пробел примечание
                        catch (Exception e) {
                            int pos = value.indexOf(' ');
                            if (pos > 0) {
                                email = value.substring(0, pos);
                                comment = value.substring(pos + 1).trim();
                            } else {
                                email = value;
                            }
                        }

                        billingValues.put(email, comment);
                    }

                    SortedMap<Integer, ParameterEmailValue> customerParamValue = paramValueDao.getParamEmail(customer.getId(), param.getId());
                    if (customerParamValue != null) {
                        // удаление из номеров параметра договора всего что уже есть в контрагенте
                        for (ParameterEmailValue item : customerParamValue.values()) {
                            billingValues.remove(item.getValue().trim());
                        }
                    }

                    for (Map.Entry<String, String> me : billingValues.entrySet()) {
                        try {
                            paramValueDao.updateParamEmail(customer.getId(), param.getId(), 0, new ParameterEmailValue(me.getKey(), me.getValue()));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }
}
