package ru.bgcrm.plugin.task.exec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.app.event.iface.Event;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.UserCache;
import org.bgerp.cache.UserNewsCache;
import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.expression.UserExpressionObject;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.event.client.NewsInfoEvent;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;
import org.bgerp.util.mail.MailMsg;

import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.task.Plugin;
import ru.bgcrm.util.sql.SingleConnectionSet;

/**
 * Sends to users info about their current states: unread messages, unprocessed news.
 * TODO: Класс UserRunner, выполняющий действия для каждого пользователя.
 *
 * @author Shamil Vakhitov
 */
@Bean(oldClasses = "ru.bgcrm.plugin.task.UserStateSender")
public class UserStateSender extends org.bgerp.app.exec.scheduler.Task {
    private static final Log log = Log.getLog();

    public static class Config extends org.bgerp.app.cfg.Config {
        private final int emailParamId;
        private final String emailExpression;

        public Config(ConfigMap setup) {
            super(setup);
            String prefix = Plugin.ID + ":userStateSender.email.";
            emailParamId = setup.getInt(prefix + "paramId", -1);
            emailExpression = setup.get(prefix + Expression.DO_EXPRESSION_CONFIG_KEY);
        }
    }

    public UserStateSender() {
        super(null);
    }

    @Override
    public String getTitle() {
        return Plugin.INSTANCE.getLocalizer().l("Task User State Sender");
    }

    @Override
    public void run() {
        Config cfg = Setup.getSetup().getConfig(Config.class);

        Parameter paramEmail = ParameterCache.getParameter(cfg.emailParamId);
        if (paramEmail == null || !Parameter.TYPE_EMAIL.equals(paramEmail.getType())) {
            log.error("Parameter with id: {} not found, or has type not email.", cfg.emailParamId);
            return;
        }

        try (var con = Setup.getSetup().getConnectionPool().getDBConnectionFromPool()) {
            ParamValueDAO paramDao = new ParamValueDAO(con);

            MailMsg msg = new MailMsg(Setup.getSetup());

            for (User user : UserCache.getActiveUsers()) {
                log.debug("Checking: {}", user.getLogin());

                Collection<ParameterEmailValue> emails = paramDao.getParamEmail(user.getId(), paramEmail.getId()).values();
                if (emails.isEmpty()) {
                    log.debug("No emails.");
                    continue;
                }

                NewsInfoEvent event = UserNewsCache.getUserEvent(new SingleConnectionSet(con), user.getId());
                log.info("Sending email to: {}", user.getLogin());

                Map<String, Object> context = new HashMap<>();
                context.put("msg", msg);
                context.put("emails", emails);
                context.put(Event.KEY, event);
                new UserExpressionObject(user).toContext(context);

                new Expression(context).execute(cfg.emailExpression);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

}
