package ru.bgcrm.plugin.bgbilling.transfer;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.user.User;

/**
 * Аккаунт, используемый в TransferData
 */
public class UserAccount extends User {
    public static final UserAccount getUserAccount(String billingId, User user) {
        ConfigMap configMap = user.getConfigMap();
        return new UserAccount(configMap.get("bgbilling:login." + billingId, configMap.get("bgbilling:login", user.getLogin())),
                configMap.get("bgbilling:password." + billingId, configMap.get("bgbilling:password", user.getPassword())));
    }

    private UserAccount(String login, String password) {
        super(login, password);
    }
}
