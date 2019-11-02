package ru.bgcrm.struts.action;

import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.SessionLogAppender;
import ru.bgcrm.util.sql.ConnectionSet;

public class LogAction extends BaseAction {
	@Override
	protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		HttpSession session = form.getHttpRequest().getSession();
		
		form.setResponseData("state", SessionLogAppender.isSessionTracked(session));
		form.setResponseData("log", SessionLogAppender.getSessionLog(session));

		return processUserTypedForward(conSet, mapping, form, FORWARD_DEFAULT);
	}
	
	public ActionForward log(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		boolean value = form.getParamBoolean("enable", false);
		
		HttpSession session = form.getHttpRequest().getSession();
		if (value)
			SessionLogAppender.trackSession(session, true);
		else
			SessionLogAppender.untrackSession(session);

		return unspecified(mapping, form, conSet);
	}
	
	public ActionForward download(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
			throws Exception {
		boolean value = form.getParamBoolean("enable", false);
		
		HttpSession session = form.getHttpRequest().getSession();
		if (value)
			SessionLogAppender.trackSession(session, true);
		else
			SessionLogAppender.untrackSession(session);

		return processJsonForward(conSet, form);
	}
}