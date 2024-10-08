package ru.bgcrm.plugin.bgbilling.struts.action;

import java.sql.Connection;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;

import javassist.NotFoundException;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.ContractTypesConfig;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.creator.Config;
import ru.bgcrm.plugin.bgbilling.creator.ServerCustomerCreator;
import ru.bgcrm.plugin.bgbilling.dao.ContractCustomerDAO;
import ru.bgcrm.plugin.bgbilling.model.ContractType;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO.SearchOptions;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractTariffDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractInfo;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.LinkAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

/**
 * Все действия, относящиеся только к манипуляции данными договора на стороне биллинга перенести в
 * {@link ru.bgcrm.plugin.bgbilling.proto.struts.action.ContractAction}. Такие методы помечены как устаревшие.
 */
@Action(path = "/user/plugin/bgbilling/contract")
public class ContractAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return contract(form, conSet);
    }

    public ActionForward customerContractList(DynActionForm form, Connection con) {
        int customerId = form.getParamInt("customerId", 0);

        form.setResponseData("list",
                new CustomerLinkDAO(con).getObjectLinksWithType(customerId, Contract.OBJECT_TYPE + "%"));
        form.setResponseData("customerId", customerId);

        form.setRequestAttribute("contractTypesConfig", setup.getConfig(ContractTypesConfig.class));
        form.setRequestAttribute("customer", new CustomerDAO(con).getCustomerById(customerId));

        return html(con, form, PATH_JSP + "/customer_contract_list.jsp");
    }

    public ActionForward contractCreateTariff(DynActionForm form, ConnectionSet conSet) throws Exception {
        var config = setup.getConfig(ContractTypesConfig.class);
        int typeId = form.getParamInt("typeId");

        var type = config.getTypeMap().get(typeId);
        if (type == null)
            throw new NotFoundException("Не найден тип договора с ID: " + typeId);

        form.setResponseData("type", type);

        return html(conSet, form, PATH_JSP + "/customer_contract_list_create_tariff.jsp");
    }

    public ActionForward contract(DynActionForm form, ConnectionSet conSet) {
        String billingId = form.getParam("billingId");
        int id = form.getId();

        if (Utils.notBlankString(billingId) && id > 0) {
            ContractInfo info = new ContractDAO(form.getUser(), billingId)
                    .getContractInfo(id);
            form.setResponseData("contract", info);

            Customer customer = new ContractCustomerDAO(conSet.getConnection()).getContractCustomer(info);
            if (customer != null) {
                form.setResponseData("customer", customer);
            }
        }

        return html(conSet, form, PATH_JSP + "/contract.jsp");
    }

    public ActionForward createCustomerFromContract(DynActionForm form, Connection con) throws BGMessageException {
        Config config = setup.getConfig(Config.class);
        String billingId = form.getParam("billingId");

        if (config == null) {
            throw new BGMessageExceptionWithoutL10n("Отсутствующая либо некорректная конфигурация импорта контрагентов.");
        }

        ServerCustomerCreator serverCustomerCreator = config.getServerCustomerCreator(billingId, con);

        if (serverCustomerCreator == null) {
            throw new BGMessageExceptionWithoutL10n("Для данного биллинга не настроен импорт контрагентов.");
        }

        serverCustomerCreator.createCustomer(billingId, con, form.getParamInt("contractId", -1),
                form.getParamInt("customerId", -1));

        return json(con, form);
    }

    public ActionForward copyCustomerParamCascade(DynActionForm form, Connection con) throws Exception {
        int customerId = form.getParamInt("customerId", -1);
        User user = form.getUser();

        ContractDAO.copyParametersToAllContracts(con, user, customerId);

        return html(con, form, PATH_JSP + "/contract.jsp");
    }

    public ActionForward copyCustomerParamToContract(DynActionForm form, Connection con) throws Exception {
        int customerId = form.getParamInt("customerId", -1);

        int contractId = form.getParamInt("contractId", -1);
        String contractTitle = form.getParam("contractTitle", "");

        ContractDAO contractDAO = new ContractDAO(form.getUser(), form.getParam("billingId"));
        contractDAO.copyParametersToBilling(con, customerId, contractId, contractTitle);

        return json(con, form);
    }

    public ActionForward contractFind(DynActionForm form, Connection con) {
        String billingId = form.getParam("billingId");
        String title = form.getParam("title");

        Pageable<IdTitle> searchResult = new Pageable<>();
        new ContractDAO(form.getUser(), billingId).searchContractByTitleComment(searchResult, title, null, null);
        form.setResponseData("contract", Utils.getFirst(searchResult.getList()));

        return json(con, form);
    }

    public ActionForward contractCreate(DynActionForm form, Connection con) throws Exception {
        int customerId = Utils.parseInt(form.getParam("customerId"));
        String billingId = form.getParam("billingId");
        int patternId = Utils.parseInt(form.getParam("patternId"));
        Date date = form.getParamDate("date");
        String titlePattern = form.getParam("titlePattern");
        String title = form.getParam("title");
        String comment = form.getParam("comment", "");
        int tariffId = form.getParamInt("tariffId");

        ContractTypesConfig config = setup.getConfig(ContractTypesConfig.class);
        ContractType type = config.getTypeMap().get(form.getParamInt("typeId"));
        if (type == null)
            throw new BGException("Не передан тип договора");

        ContractDAO contractDao = new ContractDAO(form.getUser(), billingId);

        Contract contract = contractDao.createContract(patternId, date, title, titlePattern, 0);
        if (customerId > 0) {
            CommonObjectLink link = new CommonObjectLink(Customer.OBJECT_TYPE, customerId, "contract:" + billingId,
                    contract.getId(), contract.getTitle());

            LinkAddingEvent event = new LinkAddingEvent(form, link);
            EventProcessor.processEvent(event, new SingleConnectionSet(con));

            new CustomerLinkDAO(con).addLink(link);

            new ContractDAO(form.getUser(), billingId).copyParametersToBilling(con, customerId, contract.getId(),
                    contract.getTitle());
        }

        // комментарий
        if (Utils.notBlankString(comment)) {
            contractDao.bgbillingUpdateContractTitleAndComment(contract.getId(), comment, 0);
            contract.setComment(comment);
        }

        // тариф
        if (tariffId > 0) {
            ContractTariffDAO tariffDao = new ContractTariffDAO(form.getUser(), billingId);
            int tariffPosition = type.getTariffPosition();
            if (tariffPosition < 0)
                tariffDao.setTariffPlan(contract.getId(), tariffId);
            else
                tariffDao.addTariffPlan(contract.getId(), tariffId, tariffPosition);
        }

        form.setResponseData("contract", contract);

        return json(con, form);
    }

    public ActionForward addProcessContractLink(DynActionForm form, Connection con) throws Exception {
        String billingId = form.getParam("billingId");
        int processId = form.getParamInt("processId");
        String contractTitle = form.getParam("contractTitle");

        if (Utils.isBlankString(billingId) || processId <= 0 || Utils.isBlankString(contractTitle)) {
            throw new BGIllegalArgumentException();
        }

        final SearchOptions searchOptions = new SearchOptions(true, true, true);

        Pageable<IdTitle> searchResult = new Pageable<>();
        new ContractDAO(form.getUser(), billingId)
                .searchContractByTitleComment(searchResult, "^" + contractTitle + "$", null, searchOptions);

        IdTitle result = Utils.getFirst(searchResult.getList());
        if (result == null) {
            throw new BGMessageExceptionWithoutL10n("Договор не найден");
        }

        CommonObjectLink link = new CommonObjectLink(Process.OBJECT_TYPE, processId,
                Contract.OBJECT_TYPE + ":" + billingId, result.getId(), contractTitle);
        LinkAction.addLink(form, con, link);

        return json(con, form);
    }
}