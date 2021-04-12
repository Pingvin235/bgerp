package ru.bgcrm.plugin.dispatch.struts.action;

import java.sql.Connection;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.model.Dispatch;
import ru.bgcrm.plugin.dispatch.model.DispatchMessage;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class DispatchAction extends BaseAction {
    
    public ActionForward dispatchList(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new DispatchDAO(con).searchDispatch(new SearchResult<Dispatch>(form));

        return html(con, mapping, form, "dispatchList");
    }

    public ActionForward dispatchGet(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        if (form.getId() > 0)
            form.getResponse().setData("dispatch", new DispatchDAO(con).dispatchGet(form.getId()));

        return html(con, mapping, form, "dispatchEdit");
    }

    public ActionForward dispatchUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        Dispatch dispatch = new Dispatch();

        dispatch.setId(form.getId());
        dispatch.setTitle(form.getParam("title"));
        dispatch.setComment(form.getParam("comment"));

        new DispatchDAO(con).dispatchUpdate(dispatch);

        return json(con, form);
    }

    public ActionForward messageList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
        form.getHttpRequest().setAttribute("dispatchList", new DispatchDAO(conSet.getSlaveConnection()).dispatchList(null));

        new DispatchDAO(conSet.getConnection()).messageSearch(new SearchResult<DispatchMessage>(form), form.getParamBoolean("sent", null));

        return html(conSet, mapping, form, "messageList");
    }

    public ActionForward messageGet(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
        form.getHttpRequest().setAttribute("dispatchList", new DispatchDAO(conSet.getSlaveConnection()).dispatchList(null));

        DispatchMessage message = new DispatchMessage();
        if (form.getId() > 0) {
            message = new DispatchDAO(conSet.getConnection()).messageGet(form.getId());
        }
        form.getResponse().setData("message", message);

        return html(conSet, mapping, form, "messageEdit");
    }

    public ActionForward messageUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        DispatchMessage message = new DispatchMessage();
        message.setCreateTime(new Date());

        if (form.getId() > 0)
            message = new DispatchDAO(con).messageGet(form.getId());

        message.setId(form.getId());

        if (message.getSentTime() == null) {
            message.setTitle(form.getParam("title"));
            message.setText(form.getParam("text"));
            message.setReady(form.getParamBoolean("ready", false));
            message.setDispatchIds(form.getSelectedValues("dispatchId"));

            new DispatchDAO(con).messageUpdate(message);
        } else {
            throw new BGMessageException("Сообщение было разослано, правка запрещена.");
        }

        return json(con, form);
    }
    
}
