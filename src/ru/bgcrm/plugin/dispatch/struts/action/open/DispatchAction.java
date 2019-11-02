package ru.bgcrm.plugin.dispatch.struts.action.open;

import java.sql.Connection;

import javax.mail.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.dispatch.CommandProcessor;
import ru.bgcrm.plugin.dispatch.Config;
import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.model.Dispatch;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;

public class DispatchAction extends BaseAction {
    
    public ActionForward dispatchList(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new DispatchDAO(con).searchDispatch(new SearchResult<Dispatch>(form));
        return processUserTypedForward(con, mapping, form, "list");
    }
    
    public ActionForward subscribe(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        String email = form.getParam("email");
        if (StringUtils.isBlank(email))
            throw new BGIllegalArgumentException();
        
        Config config = setup.getConfig(Config.class);
        if (!config.getMailConfig().check())
            throw new BGMessageException("Mail config is not configured!");
        
        DispatchDAO dispatchDao = new DispatchDAO(con);
        
        // generate mail text
        dispatchDao.searchDispatch(new SearchResult<Dispatch>());
        
        Session session = config.getMailConfig().getSmtpSession(Setup.getSetup());
        CommandProcessor.sendDispatchStateList(con, config, session, email);

        return processJsonForward(con, form);
    }

}
