package ru.bgcrm.plugin.mobile;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.ExpressionContextAccessingObject;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.mobile.dao.MobileDAO;
import ru.bgcrm.plugin.mobile.model.Account;
import ru.bgcrm.struts.form.DynActionForm;

public class ExpressionObject extends ExpressionContextAccessingObject {
    private static final Log log = Log.getLog();

    ExpressionObject() {}

    @Override
    public void toContext(Map<String, Object> context) {
        super.toContext(context);
        context.put(Plugin.ID, this);
    }

    /**
     * Sends mobile app push notification to executors, except ones from the current {@link DynActionForm}.
     * @param subject message subject.
     * @param text message text.
     * @throws SQLException
     */
    public void sendMessageToExecutors(String subject, String text) throws SQLException {
        Process process = (Process)context.get(ProcessExpressionObject.KEY);
        DynActionForm form = (DynActionForm)context.get(DynActionForm.KEY);

        Collection<Integer> userIds = process.getExecutorIds().stream()
            .filter(userId -> userId != form.getUserId())
            .collect(Collectors.toList());

        sendMessageToUsers(subject, text, userIds);
    }

    /**
     * Sends mobile app push notification to executors.
     * @param subject message subject.
     * @param text message text.
     * @param userIds recipient user IDs.
     * @throws SQLException
     */
    public void sendMessageToUsers(String subject, String text, Iterable<Integer> userIds) throws SQLException {
        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            GMS gms = Setup.getSetup().getConfig(GMS.class);
            for (int userId : userIds) {
                Account account = new MobileDAO(con).findAccount(User.OBJECT_TYPE, userId);
                if (account == null) {
                    log.debug("User {} isn't logged in.", userId);
                    continue;
                }
                gms.sendMessage(account.getKey(), subject, text);
            }
        }
    }
}
