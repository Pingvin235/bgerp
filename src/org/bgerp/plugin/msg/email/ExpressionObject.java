package org.bgerp.plugin.msg.email;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.l10n.Localization;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ExpressionContextAccessingObject;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.MailMsg;
import ru.bgcrm.util.sql.SingleConnectionSet;

public class ExpressionObject extends ExpressionContextAccessingObject {
    public ExpressionObject() {}

    public ExpressionObject(Process process, DynActionForm form, Connection con) throws Exception {
        this();
        setExpression(new Expression(Expression.context(new SingleConnectionSet(con), form, null, process)));
    }

    /**
     * Sends email to process' executors, except ones from the current {@link DynActionForm}.
     * @param paramId user parameter ID with type 'email', use {@code 0} for using the first user parameter with this type.
     * @param subject message subject.
     * @param text message text.
     * @throws BGMessageException
     * @throws SQLException
     */
    public void sendMessageToExecutors(int paramId, String subject, String text) throws BGMessageException, SQLException {
        Process process = (Process)expression.getContextObject(Process.OBJECT_TYPE);
        DynActionForm form = (DynActionForm)expression.getContextObject(DynActionForm.KEY);

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
     */
    public void sendMessageToUsers(Iterable<Integer> userIds, int paramId, String subject, String text) throws BGMessageException, SQLException {
        DynActionForm form = (DynActionForm)expression.getContextObject(DynActionForm.KEY);

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
                    new MailMsg(Setup.getSetup()).sendMessage(value.getValue(), subject, text);
                }
            }
        }
    }
}
