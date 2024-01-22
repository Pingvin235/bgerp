package org.bgerp.plugin.msg.email.config;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.param.Parameter;
import org.bgerp.plugin.msg.email.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.user.User;

public class ProcessNotificationConfig extends org.bgerp.app.cfg.Config {
    private static final Log log = Log.getLog();

    private final int userEmailParamId;

    protected ProcessNotificationConfig(ConfigMap config) throws InitStopException {
        super(null);
        config = config.sub(Plugin.ID + ":");
        this.userEmailParamId = loadEmailParamId(config);
        initWhen(userEmailParamId > 0);
    }

    private int loadEmailParamId(ConfigMap config) {
        int result = config.getInt("change.notification.user.email.param", -1);

        if (result == 0) {
            var param = ParameterCache.getObjectTypeParameterList(User.OBJECT_TYPE).stream()
                .filter(p -> Parameter.TYPE_EMAIL.equals(p.getType()))
                .findFirst()
                .orElse(null);

            if (param != null)
                result = param.getId();
        }

        log.debug("User 'email' parameter: {}", userEmailParamId);

        return result;
    }

    /**
     * @return user parameter type 'email' to send notifications.
     */
    public int userEmailParamId() {
        return userEmailParamId;
    }
}
