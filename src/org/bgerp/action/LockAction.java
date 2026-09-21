package org.bgerp.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGIllegalArgumentException;

import ru.bgcrm.dao.Locker;
import ru.bgcrm.model.Lock;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/lock", pathId = true)
public class LockAction extends BaseAction {
    public ActionForward add(DynActionForm form, ConnectionSet conSet) throws Exception {
        String lockId = getLockId(form);

        Locker.addLock(new Lock(lockId, form.getUserId()));

        return json(conSet, form);
    }

    public ActionForward free(DynActionForm form, ConnectionSet conSet) throws Exception {
        String lockId = getLockId(form);

        Locker.freeLock(new Lock(lockId, form.getUserId()));

        return json(conSet, form);
    }

    private String getLockId(DynActionForm form) throws BGIllegalArgumentException {
        String lockId = form.getParam("lockId");
        if (Utils.isBlankString(lockId)) {
            throw new BGIllegalArgumentException();
        }
        return lockId;
    }
}
