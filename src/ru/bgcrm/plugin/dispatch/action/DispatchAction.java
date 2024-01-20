package ru.bgcrm.plugin.dispatch.action;

import java.sql.Connection;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.plugin.dispatch.Plugin;
import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.model.Dispatch;
import ru.bgcrm.plugin.dispatch.model.DispatchMessage;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/dispatch/dispatch")
public class DispatchAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward dispatchList(DynActionForm form, Connection con) throws Exception {
        new DispatchDAO(con).searchDispatch(new Pageable<Dispatch>(form));

        return html(con, form, PATH_JSP + "/dispatch/list.jsp");
    }

    public ActionForward dispatchGet(DynActionForm form, Connection con) throws Exception {
        if (form.getId() > 0)
            form.setResponseData("dispatch", new DispatchDAO(con).dispatchGet(form.getId()));

        return html(con, form, PATH_JSP + "/dispatch/edit.jsp");
    }

    public ActionForward dispatchUpdate(DynActionForm form, Connection con) throws Exception {
        Dispatch dispatch = new Dispatch();

        dispatch.setId(form.getId());
        dispatch.setTitle(form.getParam("title"));
        dispatch.setComment(form.getParam("comment"));

        new DispatchDAO(con).dispatchUpdate(dispatch);

        return json(con, form);
    }

    public ActionForward messageList(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.getHttpRequest().setAttribute("dispatchList", new DispatchDAO(conSet.getSlaveConnection()).dispatchList(null));

        new DispatchDAO(conSet.getConnection()).messageSearch(new Pageable<DispatchMessage>(form), form.getParamBoolean("sent", null));

        return html(conSet, form, PATH_JSP + "/message/list.jsp");
    }

    public ActionForward messageGet(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.getHttpRequest().setAttribute("dispatchList", new DispatchDAO(conSet.getSlaveConnection()).dispatchList(null));

        DispatchMessage message = new DispatchMessage();
        if (form.getId() > 0) {
            message = new DispatchDAO(conSet.getConnection()).messageGet(form.getId());
        }
        form.setResponseData("message", message);

        return html(conSet, form, PATH_JSP + "/message/edit.jsp");
    }

    public ActionForward messageUpdate(DynActionForm form, Connection con) throws Exception {
        DispatchMessage message = new DispatchMessage();
        message.setCreateTime(new Date());

        if (form.getId() > 0)
            message = new DispatchDAO(con).messageGet(form.getId());

        message.setId(form.getId());

        if (message.getSentTime() == null) {
            message.setTitle(form.getParam("title"));
            message.setText(form.getParam("text"));
            message.setReady(form.getParamBoolean("ready", false));
            message.setDispatchIds(form.getParamValues("dispatchId"));

            new DispatchDAO(con).messageUpdate(message);
        } else {
            throw new BGMessageException("Сообщение было разослано, правка запрещена.");
        }

        return json(con, form);
    }

}
