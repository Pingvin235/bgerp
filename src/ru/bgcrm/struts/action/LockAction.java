package ru.bgcrm.struts.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.Locker;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.Lock;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class LockAction extends BaseAction {
    public ActionForward add(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        String lockId = getLockId(form);

        Locker.addLock(new Lock(lockId, form.getUserId()));

        return processJsonForward(conSet, form);
    }

    public ActionForward free(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        String lockId = getLockId(form);

        Locker.freeLock(new Lock(lockId, form.getUserId()));

        return processJsonForward(conSet, form);
    }

    private String getLockId(DynActionForm form) throws BGIllegalArgumentException {
        String lockId = form.getParam("lockId");
        if (Utils.isBlankString(lockId)) {
            throw new BGIllegalArgumentException();
        }
        return lockId;
    }
}
