package ru.bgcrm.plugin.bgbilling.transfer;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.UserCache;
import org.bgerp.model.user.iface.UserAccount;

import ru.bgcrm.model.user.User;

/**
 * Account used in {@link ru.bgcrm.plugin.bgbilling.TransferData}
 */
public class BillingUserAccount implements UserAccount {
    public static final UserAccount getUserAccount(String billingId, UserAccount userAccount) {
        if (userAccount instanceof User user) {
            ConfigMap configMap = user.getConfigMap();
            return new BillingUserAccount(configMap.get("bgbilling:login." + billingId, configMap.get("bgbilling:login", user.getLogin())),
                    configMap.get("bgbilling:password." + billingId, configMap.get("bgbilling:password", UserCache.password(user.getId()))));
        }
        return userAccount;
    }

    public static final BillingUserAccount getUserAccount(String login, String password) {
        return new BillingUserAccount(login, password);
    }

    private final String login;
    private final String password;

    private BillingUserAccount(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
