package ru.bgcrm.struts.action;

import java.util.Collections;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.MessageTypeCall.CallRegistration;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.News;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageCallAction extends BaseAction {
    
    public ActionForward numberRegister(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
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

    public ActionForward numberFree(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws BGException {
        getCallMessageType(form).numberFree(form.getUserId());

        return json(conSet, form);
    }

    private MessageTypeCall getCallMessageType(DynActionForm form) throws BGException {
        MessageTypeConfig config = Setup.getSetup().getConfig(MessageTypeConfig.class);

        MessageType type = config.getTypeMap().get(form.getParamInt("typeId"));
        if (type == null || !(type instanceof MessageTypeCall)) {
            throw new BGException("Не найден тип сообщений либо он не MessageTypeCall.");
        }

        return (MessageTypeCall) type;
    }
}