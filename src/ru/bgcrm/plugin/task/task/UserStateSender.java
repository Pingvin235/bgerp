package ru.bgcrm.plugin.task.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.event.client.NewsInfoEvent;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.cache.UserNewsCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.event.Event;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.task.Plugin;
import ru.bgcrm.util.MailMsg;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;

/**
 * Sends to users info about their current states: unread messages, unprocessed news.
 * TODO: Класс UserRunner, выполняющий действия для каждого пользователя.
 *
 * @author Shamil Vakhitov
 */
@Bean(oldClasses = "ru.bgcrm.plugin.task.UserStateSender")
public class UserStateSender extends org.bgerp.app.scheduler.Task {
    private static final Log log = Log.getLog();

    public static class Config extends ru.bgcrm.util.Config {
        private final int emailParamId;
        private final String emailExpression;

        public Config(ParameterMap setup) {
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
    public void run() {
        Config cfg = Setup.getSetup().getConfig(Config.class);

        Parameter paramEmail = ParameterCache.getParameter(cfg.emailParamId);
        if (paramEmail == null || !Parameter.TYPE_EMAIL.equals(paramEmail.getType())) {
            log.error("Parameter with id: {} not found, or has type not email.", cfg.emailParamId);
            return;
        }

        try (var con = Setup.getSetup().getConnectionPool().getDBSlaveConnectionFromPool()) {
            ParamValueDAO paramDao = new ParamValueDAO(con);

            MailMsg msg = new MailMsg(Setup.getSetup());

            for (User user : UserCache.getActiveUsers()) {
                log.debug("Checking: {}", user.getLogin());

                Collection<ParameterEmailValue> emails = paramDao.getParamEmail(user.getId(), paramEmail.getId()).values();
                if (emails.isEmpty()) {
                    log.debug("No emails.");
                    continue;
                }

                NewsInfoEvent event = UserNewsCache.getUserEvent(con, user.getId());
                log.info("Send email to: " + user.getLogin());

                Map<String, Object> context = new HashMap<>(10);
                context.put("msg", msg);
                context.put("emails", emails);
                context.put(Event.KEY, event);
                context.put(User.OBJECT_TYPE, user);

                new Expression(context).executeScript(cfg.emailExpression);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

}
