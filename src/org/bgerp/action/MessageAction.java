package org.bgerp.action;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.model.user.PermissionActionMethodException;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageAction extends BaseAction {
    public ActionForward modifyNotOwned(DynActionForm form, ConnectionSet conSet) {
        throw new PermissionActionMethodException();
    }
}
