package ru.bgcrm.struts.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.model.Pageable;

import ru.bgcrm.cache.CustomerGroupCache;
import ru.bgcrm.dao.CustomerGroupDAO;
import ru.bgcrm.model.customer.CustomerGroup;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/customer")
public class CustomerAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/customer/group";

    public ActionForward groupList(DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO(conSet.getConnection());
        customerGroupDAO.searchGroup(new Pageable<CustomerGroup>(form));

        return html(conSet, form, PATH_JSP + "/list.jsp");
    }

    public ActionForward groupGet(DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroup group = new CustomerGroupDAO(conSet.getConnection()).getGroupById(form.getId());
        if (group != null) {
            form.getResponse().setData("group", group);
        }

        return html(conSet, form, PATH_JSP + "/update.jsp");
    }

    public ActionForward groupUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO(conSet.getConnection());

        CustomerGroup group = new CustomerGroup();
        group.setId(form.getId());
        group.setTitle(form.getParam("title", ""));
        group.setComment(form.getParam("comment", ""));

        customerGroupDAO.updateGroup(group);

        CustomerGroupCache.flush(conSet.getConnection());

        return json(conSet, form);
    }

    public ActionForward groupDelete(DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO(conSet.getConnection());
        customerGroupDAO.deleteGroup(form.getId());

        CustomerGroupCache.flush(conSet.getConnection());

        return json(conSet, form);
    }
}