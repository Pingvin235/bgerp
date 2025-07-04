package org.bgerp.plugin.msg.email;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.l10n.Localization;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.expression.ExpressionContextAccessingObject;
import org.bgerp.dao.expression.ProcessExpressionObject;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.mail.MailMsg;

import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.SingleConnectionSet;

public class ExpressionObject extends ExpressionContextAccessingObject {
    public ExpressionObject() {}

    public ExpressionObject(Process process, DynActionForm form, Connection con) throws Exception {
        context = Expression.context(new SingleConnectionSet(con), form, null, process);
    }

    @Override
    public void toContext(Map<String, Object> context) {
        super.toContext(context);
        context.put(Plugin.ID, this);
    }

    /**
     * Sends email to process' executors, except ones from the current {@link DynActionForm}.
     * @param paramId user parameter ID with type 'email', use {@code 0} for using the first user parameter with this type.
     * @param subject message subject.
     * @param text message text.
     * @throws BGMessageException
     * @throws SQLException
     * @throws MessagingException
    */
    public void sendMessageToExecutors(int paramId, String subject, String text) throws BGMessageException, SQLException, MessagingException {
        Process process = (Process)context.get(ProcessExpressionObject.KEY);
        DynActionForm form = (DynActionForm)context.get(DynActionForm.KEY);

        Set<Integer> executorIds = process.getExecutorIds().stream()
            .filter(userId -> userId != form.getUserId())
            .collect(Collectors.toSet());

        sendMessageToUsers(executorIds, paramId, subject, text);
    }

    /**
     * Sends email to users.
     * @param userIds recipient user IDs.
     * @param paramId user parameter ID with type 'email', use {@code 0} for using the first user parameter with this type.
     * @param subject message subject.
     * @param text message text.
     * @throws BGMessageException
     * @throws SQLException
     * @throws MessagingException
     */
    public void sendMessageToUsers(Iterable<Integer> userIds, int paramId, String subject, String text) throws BGMessageException, SQLException, MessagingException {
        DynActionForm form = (DynActionForm)context.get(DynActionForm.KEY);

        Parameter param = null;
        if (paramId == 0)
            param = ParameterCache.getObjectTypeParameterList(User.OBJECT_TYPE).stream()
                .filter(p -> Parameter.TYPE_EMAIL.equals(p.getType()))
                .findFirst()
                .orElse(null);
        else
            param = ParameterCache.getParameter(paramId);

        if (param == null || !Parameter.TYPE_EMAIL.equals(param.getType()))
            throw new BGMessageException(Localization.getLocalizer(Plugin.ID, form.getHttpRequest()), "Parameter with ID {} not found or has no 'email' type.", paramId);

        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            ParamValueDAO paramDao = new ParamValueDAO(con);
            for (Integer executorId : userIds) {
                for (ParameterEmailValue value : paramDao.getParamEmail(executorId, paramId).values()) {
                    new MailMsg(Setup.getSetup()).send(value.getValue(), subject, text);
                }
            }
        }
    }
}
