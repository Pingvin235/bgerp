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

/**
 * Plugin configuration
 *
 * @author Shamil Vakhitov
 */
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
        // TODO: Parallel requesting in many threads
        for (LdapAuthConfig ldap : this.ldap) {
            var result = ldap.auth(event.getLogin(), event.getPassword());
            if (!result.isSuccess()) {
                log.debug("Login {}, unsuccessful auth by {}:{}", event.getLogin(), ldap, result.getException());
                continue;
            }

            log.debug("Login {}, successful auth by {}", event.getLogin(), ldap);
            event.setProcessed(true);

            createOrUpdateUser(event, result);

            UserCache.password(event.getUser().getId(), event.getPassword());

            break;
        }
    }

    private void createOrUpdateUser(AuthEvent event, AuthResult result) {
        var user = event.getUser();
        if (user == null) {
            user = result.getUser();

            log.info("Creating new user with login: {}", user.getLogin());

            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                var dao = new UserDAO(con);

                user.setGroupIds(result.getUser().getGroupIds());
                dao.updateUser(user);

                event.setUser(user);

                if (result.hasUpdateExpression())
                    result.doUpdateExpression(con, user);

                UserCache.flush(con);
            } catch (SQLException e) {
                log.error(e);
            }
        } else {
            boolean titleUpdate = !user.getTitle().equals(result.getUser().getTitle());
            boolean groupsUpdate = !user.getGroupIds().equals(result.getUser().getGroupIds());
            boolean updateExpression = result.hasUpdateExpression();

            if (titleUpdate || groupsUpdate || updateExpression) {
                log.info("Updating user with login: {}, title: {}, groups: {}, expression: {}", user.getLogin(), titleUpdate, groupsUpdate,
                        updateExpression);

                try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                    var dao = new UserDAO(con);

                    if (titleUpdate) {
                        user.setTitle(result.getUser().getTitle());
                        dao.updateUser(user);
                    }

                    if (groupsUpdate)
                        dao.updateUserGroups(user.getId(), user.getGroupIds(), result.getUser().getGroupIds());

                    if (updateExpression)
                        result.doUpdateExpression(con, user);

                    UserCache.flush(con);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }
}
