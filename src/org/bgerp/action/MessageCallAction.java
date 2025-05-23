package org.bgerp.action;

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.message.call.CallRegistration;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;

import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.model.News;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/message/call", pathId = true)
public class MessageCallAction extends BaseAction {

    public ActionForward numberRegister(DynActionForm form, ConnectionSet conSet) throws Exception {
        MessageTypeCall type = getCallMessageType(form);

        String number = form.getParam("number");
        if (Utils.isBlankString(number)) {
            throw new BGIllegalArgumentException();
        }

        final CallRegistration reg = type.getRegistrationByNumber(number);

        if (form.getParamBoolean("check", true)) {
            if (reg != null) {
                User user = UserCache.getUser(reg.getUserId());
                form.setResponseData("regUser", new IdTitle(user.getId(), user.getTitle()));
            }
        } else {
            if (reg != null) {
                News news = new News();
                news.setUserId(User.USER_SYSTEM_ID);
                news.setPopup(true);
                news.setLifeTime(1);
                news.setTitle(l.l("The number is occupied"));
                news.setDescription(l.l("Number {} occupied by user {}", number, form.getUser().getTitle()));

                new NewsDAO(conSet.getConnection()).updateNewsUsers(news, Collections.singleton(reg.getUserId()));

                type.numberFree(reg.getUserId());
            }

            type.numberRegister(form.getUserId(), number);
        }

        return json(conSet, form);
    }

    public ActionForward numberFree(DynActionForm form, ConnectionSet conSet) {
        getCallMessageType(form).numberFree(form.getUserId());

        return json(conSet, form);
    }

    public ActionForward outCall(DynActionForm form, ConnectionSet conSet) throws Exception {
        var type = getCallMessageType(form);

        int processId = form.getParamInt("processId");
        String number = form.getParam("number", Utils::notBlankString);

        var reg = type.getRegistrationByUser(form.getUserId());
        if (reg != null)
            reg.outCall(number, processId);

        return json(conSet, form);
    }

    public ActionForward testCall(DynActionForm form, Connection con) throws Exception {
        var type = getCallMessageType(form);

        var reg = getRegistrationOrThrow(form, type);

        var message = new Message();
        message.setDirection(Message.DIRECTION_INCOMING);
        message.setTypeId(type.getId());
        message.setUserId(reg.getUserId());
        message.setText("");
        message.setFrom(form.getParam("testCallFrom", "+734702"));
        message.setTo(reg.getNumber());
        message.setFromTime(new Date());
        message.setSystemId(String.valueOf(System.currentTimeMillis()));

        type.updateMessage(con, form, message);

        reg.setMessageForOpen(message);

        return json(con, form);
    }

    private CallRegistration getRegistrationOrThrow(DynActionForm form, MessageTypeCall type) throws BGMessageException {
        var reg = type.getRegistrationByUser(form.getUserId());
        if (reg == null)
            throw new BGMessageException(l.l("The user doesn't occupy a number"));
        return reg;
    }

    private MessageTypeCall getCallMessageType(DynActionForm form) {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        int typeId = form.getParamInt("typeId");
        var type = config.getTypeMap().get(typeId);
        if (type == null || !(type instanceof MessageTypeCall)) {
            throw new BGException("Not found MessageTypeCall with ID: " + typeId);
        }

        return (MessageTypeCall) type;
    }
}