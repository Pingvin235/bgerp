package org.bgerp.plugin.sec.auth.config;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import com.google.common.annotations.VisibleForTesting;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.expression.Expression;
import org.bgerp.plugin.sec.auth.AuthResult;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

/**
 * LDAP auth
 *
 * @author Shamil Vakhitov
 */
public class LdapAuthConfig extends Config {
    private static final Log log = Log.getLog();

    private final String title;
    private final String url;
    private final String loginExpression;
    private final String searchBase;
    private final String searchExpression;
    private final String searchAttributes;
    private final String groupIdsExpression;
    private final String titleExpression;

    LdapAuthConfig(int id, ConfigMap config) throws InitStopException {
        super(null);

        this.title = config.get("title", String.valueOf(id));
        this.url = config.get("url");
        this.loginExpression = config.get("login.expression");
        this.searchBase = config.get("search.base");
        this.searchExpression = config.get("search.expression");
        this.searchAttributes = config.get("search.attributes");
        this.groupIdsExpression = config.get("group.ids.expression");
        this.titleExpression = config.get("title.expression", "attrs.value(\"name\")");

        initWhen(Utils.notBlankStrings(url, loginExpression, searchBase, searchExpression, groupIdsExpression,
                titleExpression));
    }

    public AuthResult auth(String login, String password) {
        try {
            String searchFilter = new Expression(Map.of("login", login)).executeGetString(this.searchExpression);
            Attributes attrs = searchAttributes(login, password, searchFilter);
            return new AuthResult(user(login, password, new LDAPAttributes(attrs)));
        } catch (CommunicationException e) {
            log.error("LDAP communication exception", e);
            return new AuthResult(e.getCause());
        } catch (Exception e) {
            log.debug("LDAP auth exception", e);
            return new AuthResult(e);
        }
    }

    private User user(String login, String password, LDAPAttributes attrs) throws NamingException {
        log.debug("Found LDAP attributes for login {}: {}", login, attrs);

        var result = new User(login, "");
        result.setStatus(User.STATUS_EXTERNAL);
        result.setTitle(new Expression(Map.of("attrs", attrs)).executeGetString(titleExpression));
        result.setGroupIds(getGroupIds(attrs));

        return result;
    }

    @VisibleForTesting
    protected Attributes searchAttributes(String login, String password, String searchFilter) throws NamingException, NotFoundException {
        DirContext context = new InitialDirContext(environment(login, password));
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        if (Utils.notBlankString(searchAttributes))
            constraints.setReturningAttributes(Utils.toList(searchAttributes).toArray(new String[] {}));
        var answer = context.search(searchBase, searchFilter, constraints );
        context.close();

        if (answer.hasMore()) {
            return answer.next().getAttributes();
        } else
            throw new NotFoundException("User not found");
    }

    private Hashtable<String, String> environment(String login, String password) {
        Hashtable<String, String> result = new Hashtable<>();

        result.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        result.put(Context.PROVIDER_URL, url);
        result.put(Context.SECURITY_AUTHENTICATION, "simple");
        result.put(Context.SECURITY_PRINCIPAL, new Expression(Map.of("login", login)).executeGetString(loginExpression));
        result.put(Context.SECURITY_CREDENTIALS, password);

        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<Integer> getGroupIds(LDAPAttributes attrs) throws NamingException {
        var result = (Set<Integer>) new Expression(Map.of("attrs", attrs)).execute(groupIdsExpression);
        return Collections.unmodifiableSet(result);
    }

    @Override
    public String toString() {
        return LdapAuthConfig.class.getSimpleName() + " [" + title + "]";
    }
}
