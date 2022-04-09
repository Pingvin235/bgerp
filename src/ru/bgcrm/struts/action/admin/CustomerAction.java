package ru.bgcrm.struts.action.admin;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.model.Pageable;

import ru.bgcrm.cache.CustomerGroupCache;
import ru.bgcrm.dao.CustomerGroupDAO;
import ru.bgcrm.model.customer.CustomerGroup;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class CustomerAction extends BaseAction {
    public CustomerAction() {
        super();
    }

    public ActionForward groupList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO(conSet.getConnection());
        customerGroupDAO.searchGroup(new Pageable<CustomerGroup>(form));

        return mapping.findForward("groupList");
    }

    public ActionForward groupGet(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroup group = new CustomerGroupDAO(conSet.getConnection()).getGroupById(form.getId());
        if (group != null) {
            form.getResponse().setData("group", group);
        }

        return html(conSet, mapping, form, "groupUpdate");
    }

    public ActionForward groupUpdate(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO(conSet.getConnection());

        CustomerGroup group = new CustomerGroup();
        group.setId(form.getId());
        group.setTitle(form.getParam("title", ""));
        group.setComment(form.getParam("comment", ""));

        customerGroupDAO.updateGroup(group);

        CustomerGroupCache.flush(conSet.getConnection());

        return json(conSet, form);
    }

    public ActionForward groupDelete(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO(conSet.getConnection());
        customerGroupDAO.deleteGroup(form.getId());

        CustomerGroupCache.flush(conSet.getConnection());

        return json(conSet, form);
    }
}