package org.bgerp.plugin.sec.access.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/access/credential")
public class CredentialAction extends org.bgerp.action.BaseAction {
    public ActionForward get(DynActionForm form, ConnectionSet conSet) throws Exception {
        var user = UserCache.getUser(form.getId());
        if (user == null) {
            throw new BGException("User not found: " + form.getId());
        }

        String text = l.l("credentials.get.text", user.getTitle(), user.getLogin(), user.getPassword());
        form.setResponseData("text", text);

        return json(conSet, form);
    }

    public ActionForward sendToUser(DynActionForm form, ConnectionSet conSet) throws Exception {
        var userId = form.getId();
        if (UserCache.getUser(userId) == null) {
            throw new BGException("User not found: " + userId);
        }

        var emailParam = ParameterCache.getObjectTypeParameterList(User.OBJECT_TYPE).stream()
            .filter(p -> p.getType().equals(Parameter.TYPE_EMAIL))
            .findFirst().orElse(null);
        if (emailParam == null) {
            throw new BGMessageException("Не найден параметр пользователя с типом 'email'");
        }

        var emailValue = Utils.getFirst(new ParamValueDAO(conSet.getSlaveConnection()).getParamEmail(userId, emailParam.getId()).values());
        if (emailValue == null || Utils.isBlankString(emailValue.getValue())) {
            throw new BGMessageException("E-Mail не определён");
        }

        // send mail, use MailMsg

        return json(conSet, form);
    }
}
