package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Action(path = "/user/search")
public class SearchAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/search";

    private static final String JSP_DEFAULT = PATH_JSP + "/search.jsp";
    private static final String JSP_CUSTOMER_TITLE = PATH_JSP + "/result/customer_title.jsp";
    private static final String JSP_PROCESS = PATH_JSP + "/result/process.jsp";

    @Override
    public ActionForward unspecified(DynActionForm form, Connection con) throws Exception {
        // areas supported only in bgbilling plugin, contract search
        HashSet<Integer> areaIds = new HashSet<Integer>();

        ConfigMap perm = form.getPermission();
        Set<Integer> allowedAreaIds = Utils.toIntegerSet(perm.get("allowedAreaIds", ""));

        if (!allowedAreaIds.isEmpty())
            areaIds.retainAll(allowedAreaIds);

        form.getResponse().setData("areas", areaIds);

        return html(con, form, JSP_DEFAULT);
    }

    public ActionForward customerSearch(DynActionForm form, Connection con) throws Exception {
        CustomerDAO customerDao = new CustomerDAO(con);

        String searchBy = form.getParam("searchBy");

        if ("id".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<Customer>(form);

            int id = Utils.parseInt(form.getParam("id"));

            Customer customer = customerDao.getCustomerById(id);
            if (customer != null) {
                result.getList().add(customer);
                result.getPage().setRecordCount(1);
            } else {
                result.getPage().setRecordCount(0);
            }

            return html(con, form, JSP_CUSTOMER_TITLE);
        } else if ("title".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<Customer>(form);

            String title = form.getParam("title", "");

            long minLength = setup.getSokLong(0L, "search.customer.title.min.substring.length", "searchCustomerTitleMinSubstringLength");
            if (title.length() < minLength)
                throw new BGMessageException("Search string must be {} or more chars!", minLength);

            customerDao.searchCustomerList(result, LikePattern.SUB.get(title));

            return html(con, form, JSP_CUSTOMER_TITLE);
        } else if ("group".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<Customer>(form);

            customerDao.searchCustomerList(result, form.getSelectedValues("groupId"));

            return html(con, form, JSP_CUSTOMER_TITLE);
        } else if ("address".equals(searchBy)) {
            Pageable<ParameterSearchedObject<Customer>> result = new Pageable<ParameterSearchedObject<Customer>>(form);

            int streetId = Utils.parseInt(form.getParam("streetId"));
            String house = form.getParam("house");
            String flat = form.getParam("flat");
            String room = form.getParam("room");

            customerDao.searchCustomerListByAddress(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                    streetId, house, flat, room);

            return html(con, form, PATH_JSP + "/result/customer_address.jsp");
        } else if ("email".equals(searchBy)) {
            Pageable<ParameterSearchedObject<Customer>> result = new Pageable<ParameterSearchedObject<Customer>>(form);

            String email = form.getParam("email");

            customerDao.searchCustomerListByEmail(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                    email);

            return html(con, form, JSP_CUSTOMER_TITLE);
        } else if ("phone".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<Customer>(form);
            String phone = form.getParam("phone");

            List<Integer> paramIds = Utils.toIntegerList(form.getParam("phoneParamIds"));
            if (paramIds.size() > 0) {
                customerDao.searchCustomerListByPhone(result, paramIds, phone);
            } else {
                customerDao.searchCustomerListByPhone(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                        phone);
            }

            return html(con, form, JSP_CUSTOMER_TITLE);
        } else if ("linkedObjectTitle".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<Customer>(form);

            String linkedObjectTitle = form.getParam("linkedObjectTitle");
            String linkedObjectTypeLike = form.getParam("linkedObjectTypeLike");

            customerDao.searchCustomerByLinkedObjectTitle(result, linkedObjectTypeLike, linkedObjectTitle);

            return html(con, form, JSP_CUSTOMER_TITLE);
        }

        return html(con, form, JSP_DEFAULT);
    }

    public ActionForward processSearch(DynActionForm form, Connection con) throws Exception {
        ProcessDAO processDao = new ProcessDAO(con, form);

        String searchBy = form.getParam("searchBy");

        if ("userId".equals(searchBy)) {
            int mode = form.getParamInt("mode");

            processDao.searchProcessListForUser(new Pageable<Process>(form), form.getUserId(), mode);

            return html(con, form, JSP_PROCESS);
        } else if ("id".equals(searchBy)) {
            Pageable<Process> result = new Pageable<>(form);

            Process process = processDao.getProcess(form.getId());
            if (process != null) {
                result.getList().add(process);
                result.getPage().setRecordCount(1);
            }

            return html(con, form, JSP_PROCESS);
        }

        return html(con, form, JSP_DEFAULT);
    }

}
