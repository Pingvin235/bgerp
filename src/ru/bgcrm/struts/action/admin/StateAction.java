package ru.bgcrm.struts.action.admin;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.servlet.LoginStat;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.sql.ConnectionSet;

public class StateAction
    extends BaseAction
{
    @Override
    protected ActionForward unspecified( ActionMapping mapping, 
                                         DynActionForm form, 
                                         ConnectionSet conSet )
        throws Exception
    {
        form.getResponse().setData("status", AdminPortListener.getStatus());
        form.getResponse().setData("logged", LoginStat.getLoginStat().getLoggedUserWithSessions());
        
        return processUserTypedForward( conSet, mapping, form, FORWARD_DEFAULT );
    }
}