package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.MessageTypeCall.CallRegistration;
import ru.bgcrm.model.News;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.message.config.MessageTypeConfig;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/messageCall")
public class MessageCallAction extends BaseAction {

    public ActionForward numberRegister(DynActionForm form, ConnectionSet conSet) throws Exception {
        MessageTypeCall type = getCallMessageType(form);

        String number = form.getParam("number");
        if (Utils.isBlankString(number)) {
            throw new BGIllegalArgumentException();
        }

        CallRegistration reg = type.getRegistrationByNumber(number);

        boolean check = form.getParamBoolean("check", true);
        if (check && reg != null) {
            User user = UserCache.getUser(reg.getUserId());
            form.getResponse().setData("regUser", new IdTitle(user.getId(), user.getTitle()));
        } else {
            if (reg != null) {
                News news = new News();
                news.setUserId(User.USER_SYSTEM_ID);
                news.setPopup(true);
                news.setLifeTime(1);
                news.setTitle("Ваш номер занят");
                news.setDescription("Пользователь " + form.getUser().getTitle() + " занял ваш номер " + number);

                new NewsDAO(conSet.getConnection()).updateNewsUsers(news, Collections.singleton(reg.getUserId()));

                type.numberFree(reg.getUserId());
            }

            type.numberRegister(form.getUserId(), number);
        }

        return json(conSet, form);
    }

    public ActionForward numberFree(DynActionForm form, ConnectionSet conSet) throws BGException {
        getCallMessageType(form).numberFree(form.getUserId());

        return json(conSet, form);
    }

    public ActionForward testCall(DynActionForm form, Connection con) throws Exception {
        var type = getCallMessageType(form);

        var reg = type.getRegistrationByUser(form.getUserId());
        if (reg == null)
            throw new BGException("Пользователь не занимает номер.");

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

    private MessageTypeCall getCallMessageType(DynActionForm form) throws BGException {
        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        int typeId = form.getParamInt("typeId");
        var type = config.getTypeMap().get(typeId);
        if (type == null || !(type instanceof MessageTypeCall)) {
            throw new BGException("Not found MessageTypeCall with ID: " + typeId);
        }

        return (MessageTypeCall) type;
    }
}