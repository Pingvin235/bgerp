package org.bgerp.plugin.sec.auth.config;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.cache.UserCache;
import org.bgerp.event.AuthEvent;
import org.bgerp.plugin.sec.auth.AuthResult;
import org.bgerp.plugin.sec.auth.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.dao.user.UserDAO;

public class Config extends org.bgerp.app.cfg.Config {
    private static final Log log = Log.getLog();

    private final List<LdapAuthConfig> ldap;

    protected Config(ConfigMap config, boolean validate) throws InitStopException {
        super(config, validate);
        this.ldap = ldap(config, validate);
        initWhen(!ldap.isEmpty());
    }

    private List<LdapAuthConfig> ldap(ConfigMap config, boolean validate) {
        var result = new ArrayList<LdapAuthConfig>();
        for (var me : config.subIndexed(Plugin.ID + ":ldap.").entrySet()) {
            try {
                result.add(new LdapAuthConfig(me.getKey(), me.getValue()));
            } catch (InitStopException e) {}
        }
        log.debug("Loaded {} LDAP configurations", result.size());
        return Collections.unmodifiableList(result);
    }

    public void auth(AuthEvent event) {
        log.debug("Processing auth, login: {}", event.getLogin());
        // TODO: Parallel requesting in many threads.
        for (LdapAuthConfig ldap : this.ldap) {
            var result = ldap.auth(event.getLogin(), event.getPassword());
            if (!result.isSuccess()) {
                log.debug("Login {}, unsuccessful auth by {}:{}", event.getLogin(), ldap, result.getException());
                continue;
            }

            log.debug("Login {}, successful auth by {}", event.getLogin(), ldap);
            event.setProcessed(true);

            createOrUpdateUser(event, result);
            break;
        }
    }

    private void createOrUpdateUser(AuthEvent event, AuthResult result) {
        var user = event.getUser();
        if (user == null
            || !user.getGroupIds().equals(result.getUser().getGroupIds())
            || !user.getTitle().equals(result.getUser().getTitle())) {
            log.debug("Create or update user {} from {}", user, result.getUser());

            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                var dao = new UserDAO(con);
                if (user == null)
                    user = result.getUser();

                user.setTitle(result.getUser().getTitle());
                dao.updateUser(user);
                user.setGroupIds(result.getUser().getGroupIds());
                dao.updateUserGroups(user);

                // TODO: Duplicated groups!!
                // Title with :
                event.setUser(user);

                UserCache.flush(con);
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }
}
