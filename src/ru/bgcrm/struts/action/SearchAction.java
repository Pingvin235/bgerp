package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.bgbilling.CommonContractConfig;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class SearchAction extends BaseAction {
    public SearchAction() {
        super();
    }

    public ActionForward customerSearch(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        CustomerDAO customerDao = new CustomerDAO(con);

        String searchBy = form.getParam("searchBy");

        if ("id".equals(searchBy)) {
            SearchResult<Customer> result = new SearchResult<Customer>(form);

            int id = Utils.parseInt(form.getParam("id"));

            Customer customer = customerDao.getCustomerById(id);
            if (customer != null) {
                result.getList().add(customer);
                result.getPage().setRecordCount(1);
            } else {
                result.getPage().setRecordCount(0);
            }

            return data(con, mapping, form, "customerTitle");
        } else if ("title".equals(searchBy)) {
            SearchResult<Customer> result = new SearchResult<Customer>(form);

            String title = form.getParam("title");
            customerDao.searchCustomerList(result, CommonDAO.getLikePattern(title, "subs"));

            return data(con, mapping, form, "customerTitle");
        } 
        /* else if ("any".equals(searchBy)) {
            SearchResult<Customer> result = new SearchResult<Customer>(form);
            String searchString = form.getParam("searchString");

            SphinxDAO sphinxDAO = new SphinxDAO(con);
            sphinxDAO.searchCustomer(result, searchString);

            return processUserTypedForward(con, mapping, form, "customerTitle");
        } */ 
        else if ("group".equals(searchBy)) {
            SearchResult<Customer> result = new SearchResult<Customer>(form);

            customerDao.searchCustomerList(result, form.getSelectedValues("groupId"));

            return data(con, mapping, form, "customerTitle");
        } else if ("address".equals(searchBy)) {
            SearchResult<ParameterSearchedObject<Customer>> result = new SearchResult<ParameterSearchedObject<Customer>>(form);

            int streetId = Utils.parseInt(form.getParam("streetId"));
            String house = form.getParam("house");
            String flat = form.getParam("flat");
            String room = form.getParam("room");

            customerDao.searchCustomerListByAddress(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                    streetId, house, flat, room);

            return data(con, mapping, form, "customerAddress");
        } else if ("email".equals(searchBy)) {
            SearchResult<ParameterSearchedObject<Customer>> result = new SearchResult<ParameterSearchedObject<Customer>>(form);

            String email = form.getParam("email");

            customerDao.searchCustomerListByEmail(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                    email);

            return data(con, mapping, form, "customerTitle");
        } else if ("phone".equals(searchBy)) {
            SearchResult<Customer> result = new SearchResult<Customer>(form);
            String phone = form.getParam("phone");

            List<Integer> paramIds = Utils.toIntegerList(form.getParam("phoneParamIds"));
            if (paramIds.size() > 0) {
                customerDao.searchCustomerListByPhone(result, paramIds, phone);
            } else {
                customerDao.searchCustomerListByPhone(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                        phone);
            }

            return data(con, mapping, form, "customerTitle");
        } else if ("linkedObjectTitle".equals(searchBy)) {
            SearchResult<Customer> result = new SearchResult<Customer>(form);

            String linkedObjectTitle = form.getParam("linkedObjectTitle");
            String linkedObjectTypeLike = form.getParam("linkedObjectTypeLike");

            customerDao.searchCustomerByLinkedObjectTitle(result, linkedObjectTypeLike, linkedObjectTitle);

            return data(con, mapping, form, "customerTitle");
        }

        return mapping.findForward(FORWARD_DEFAULT);
    }

    public ActionForward processSearch(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form.getUser());

        String searchBy = form.getParam("searchBy");

        if ("userId".equals(searchBy)) {
            int mode = form.getParamInt("mode");

            processDao.searchProcessListForUser(new SearchResult<Process>(form), form.getUserId(), mode);

            return data(con, mapping, form, "process");
        } else if ("id".equals(searchBy)) {
            SearchResult<Process> result = new SearchResult<>(form);

            Process process = processDao.getProcess(form.getId());
            if (process != null) {
                result.getList().add(process);
                result.getPage().setRecordCount(1);
            }

            return data(con, mapping, form, "process");
        }

        return mapping.findForward(FORWARD_DEFAULT);
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        HashSet<Integer> areaIds = new HashSet<Integer>();
        areaIds.addAll(setup.getConfig(CommonContractConfig.class).getCityAreaMap());

        ParameterMap perm = form.getPermission();
        Set<Integer> allowedAreaIds = Utils.toIntegerSet(perm.get("allowedAreaIds", ""));

        if (!allowedAreaIds.isEmpty())
            areaIds.retainAll(allowedAreaIds);

        form.getResponse().setData("areas", areaIds);

        return mapping.findForward(FORWARD_DEFAULT);
    }
}
