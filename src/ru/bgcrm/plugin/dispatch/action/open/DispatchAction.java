package ru.bgcrm.plugin.dispatch.action.open;

import java.sql.Connection;

import javax.mail.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.app.cfg.Setup;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.plugin.dispatch.Plugin;
import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.exec.DispatchCommandProcessor;
import ru.bgcrm.plugin.dispatch.exec.Config;
import ru.bgcrm.plugin.dispatch.model.Dispatch;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;

@Action(path = "/open/plugin/dispatch/dispatch")
public class DispatchAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_OPEN;

    public ActionForward dispatchList(DynActionForm form, Connection con) throws Exception {
        new DispatchDAO(con).searchDispatch(new Pageable<Dispatch>(form));
        return html(con, form, PATH_JSP + "/list.jsp");
    }

    public ActionForward subscribe(DynActionForm form, Connection con) throws Exception {
        String email = form.getParam("email");
        if (StringUtils.isBlank(email))
            throw new BGIllegalArgumentException();

        Config config = setup.getConfig(Config.class);
        if (!config.getMailConfig().check())
            throw new BGMessageException("Mail config is not configured!");

        DispatchDAO dispatchDao = new DispatchDAO(con);

        // generate mail text
        dispatchDao.searchDispatch(new Pageable<Dispatch>());

        Session session = config.getMailConfig().getSmtpSession(Setup.getSetup());
        DispatchCommandProcessor.sendDispatchStateList(con, config, session, email);

        return json(con, form);
    }

}
