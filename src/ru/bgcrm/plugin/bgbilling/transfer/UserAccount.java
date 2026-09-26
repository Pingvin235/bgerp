package ru.bgcrm.plugin.bgbilling.transfer;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.UserCache;

import ru.bgcrm.model.user.User;

/**
 * Account used in {@link ru.bgcrm.plugin.bgbilling.TransferData}
 */
public record UserAccount(String login,String password) {
    public static final UserAccount getUserAccount(String billingId, User user) {
        ConfigMap configMap = user.getConfigMap();
        return new UserAccount(configMap.get("bgbilling:login." + billingId, configMap.get("bgbilling:login", user.getLogin())),
                configMap.get("bgbilling:password." + billingId, configMap.get("bgbilling:password", UserCache.password(user.getId()))));
    }
}
