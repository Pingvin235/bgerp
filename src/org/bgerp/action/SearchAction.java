package org.bgerp.action;

import java.sql.Connection;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.customer.CustomerParamSearchDAO;
import org.bgerp.dao.process.ProcessParamSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.param.Parameter.Type;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/search", pathId = true)
public class SearchAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/search";
    private static final String JSP_DEFAULT = PATH_JSP + "/search.jsp";

    private static final String PATH_JSP_CUSTOMER = PATH_JSP + "/customer";
    private static final String JSP_CUSTOMER = PATH_JSP_CUSTOMER + "/customer.jsp";
    private static final String JSP_CUSTOMER_PARAM = PATH_JSP_CUSTOMER + "/param.jsp";

    private static final String PATH_JSP_PROCESS = PATH_JSP + "/process";
    private static final String JSP_PROCESS = PATH_JSP_PROCESS + "/process.jsp";
    private static final String JSP_PROCESS_PARAM = PATH_JSP_PROCESS + "/param.jsp";

    @Override
    public ActionForward unspecified(DynActionForm form, Connection con) throws Exception {
        form.setRequestAttribute("customerParamTextList", ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE).stream()
            .filter(p -> p.getTypeType() == Type.TEXT)
            .collect(Collectors.toList())
        );
        form.setRequestAttribute("processParamTextList", ParameterCache.getObjectTypeParameterList(Process.OBJECT_TYPE).stream()
            .filter(p -> p.getTypeType() == Type.TEXT)
            .collect(Collectors.toList())
        );

        return html(con, form, JSP_DEFAULT);
    }

    public ActionForward customerSearch(DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerDAO customerDao = new CustomerDAO(conSet.getSlaveConnection());

        String searchBy = form.getParam("searchBy");

        if ("id".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<>(form);

            int id = Utils.parseInt(form.getParam("id"));

            Customer customer = customerDao.getCustomerById(id);
            if (customer != null) {
                result.getList().add(customer);
                result.getPage().setRecordCount(1);
            } else {
                result.getPage().setRecordCount(0);
            }

            return html(conSet, form, JSP_CUSTOMER);
        } else if ("title".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<>(form);

            String title = form.getParam("title", "");

            long minLength = setup.getSokLong(0L, "search.customer.title.min.substring.length", "searchCustomerTitleMinSubstringLength");
            if (title.length() < minLength)
                throw new BGMessageException("Search string must be {} or more chars!", minLength);

            customerDao.searchCustomerList(result, LikePattern.SUB.get(title));

            return html(conSet, form, JSP_CUSTOMER);
        } /* else if ("group".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<>(form);

            customerDao.searchCustomerList(result, form.getParamValues("groupId"));

            return html(con, form, JSP_CUSTOMER);
        } */ else if ("address".equals(searchBy)) {
            Pageable<ParameterSearchedObject<Customer>> result = new Pageable<>(form);

            int streetId = Utils.parseInt(form.getParam("streetId"));
            String house = form.getParam("house");
            String flat = form.getParam("flat");
            String room = form.getParam("room");

            customerDao.searchCustomerListByAddress(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                    streetId, house, flat, room);

            return html(conSet, form, JSP_CUSTOMER_PARAM);
        } else if ("text".equals(searchBy)) {
            Pageable<ParameterSearchedObject<Customer>> result = new Pageable<>(form);

            String text = form.getParam("text");

            if (Utils.notBlankString(text))
                new CustomerParamSearchDAO(conSet.getSlaveConnection())
                    .withParamTextValue(LikePattern.of(form.getParam("textLikeMode")).get(text))
                    .withParamTextIds(form.getParamValues("textParam"))
                    .search(result);

            return html(conSet, form, JSP_CUSTOMER_PARAM);
        } /* else if ("email".equals(searchBy)) {
            Pageable<ParameterSearchedObject<Customer>> result = new Pageable<>(form);

            String email = form.getParam("email");

            customerDao.searchCustomerListByEmail(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                    email);

            return html(con, form, JSP_CUSTOMER_PARAM);
        } else if ("phone".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<>(form);
            String phone = form.getParam("phone");

            List<Integer> paramIds = Utils.toIntegerList(form.getParam("phoneParamIds"));
            if (paramIds.size() > 0) {
                customerDao.searchCustomerListByPhone(result, paramIds, phone);
            } else {
                customerDao.searchCustomerListByPhone(result, Utils.getObjectIdsList(ParameterCache.getObjectTypeParameterList(Customer.OBJECT_TYPE)),
                        phone);
            }

            return html(con, form, JSP_CUSTOMER_PARAM);
        } else if ("linkedObjectTitle".equals(searchBy)) {
            Pageable<Customer> result = new Pageable<>(form);

            String linkedObjectTitle = form.getParam("linkedObjectTitle");
            String linkedObjectTypeLike = form.getParam("linkedObjectTypeLike");

            customerDao.searchCustomerByLinkedObjectTitle(result, linkedObjectTypeLike, linkedObjectTitle);

            return html(con, form, JSP_CUSTOMER);
        }*/

        return html(conSet, form, JSP_DEFAULT);
    }

    public ActionForward customerSearchProcessLink(DynActionForm form, ConnectionSet conSet) throws Exception {
        customerSearch(form, conSet);

        return html(conSet, form, PATH_JSP_USER + "/process/process/link/list_search_customer.jsp");
    }

    public ActionForward processSearch(DynActionForm form, ConnectionSet conSet) throws Exception {
        ProcessDAO processDao = new ProcessDAO(conSet.getSlaveConnection(), form);

        String searchBy = form.getParam("searchBy");

        if ("userId".equals(searchBy)) {
            int mode = form.getParamInt("mode");

            processDao.searchProcessListForUser(new Pageable<>(form), form.getUserId(), mode);

            return html(conSet, form, JSP_PROCESS);
        } else if ("id".equals(searchBy)) {
            Pageable<Process> result = new Pageable<>(form);

            Process process = processDao.getProcess(form.getId());
            if (process != null) {
                result.getList().add(process);
                result.getPage().setRecordCount(1);
            }

            return html(conSet, form, JSP_PROCESS);
        } else if ("text".equals(searchBy)) {
            Pageable<ParameterSearchedObject<Process>> result = new Pageable<>(form);

            String text = form.getParam("text");

            if (Utils.notBlankString(text))
                new ProcessParamSearchDAO(conSet.getSlaveConnection(), form)
                    .withOpen(form.getParamBoolean("open", null))
                    .withParamTextValue(LikePattern.of(form.getParam("textLikeMode")).get(text))
                    .withParamTextIds(form.getParamValues("textParam"))
                    .search(result);

            return html(conSet, form, JSP_PROCESS_PARAM);
        }

        return html(conSet, form, JSP_DEFAULT);
    }

}
