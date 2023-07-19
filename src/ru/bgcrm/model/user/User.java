package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.util.PswdUtil;
import ru.bgcrm.util.Utils;

/**
 * Application user.
 *
 * @author Shamil Vakhitov
 */
public class User extends IdTitle implements Comparable<User>, Cloneable, UserAccount {
    private static final Log log = Log.getLog();

    public static final String OBJECT_TYPE = "user";

    public static final int USER_SYSTEM_ID = 0;
    public static final int USER_CUSTOMER_ID = -1;

    public static final User USER_SYSTEM = new User();
    public static final User USER_CUSTOMER = new User();

    static {
        USER_SYSTEM.setId(USER_SYSTEM_ID);
        USER_SYSTEM.setLogin("SYSTEM");
        USER_SYSTEM.setTitle("System");

        USER_CUSTOMER.setId(USER_CUSTOMER_ID);
        USER_CUSTOMER.setLogin("CUSTOMER");
        USER_CUSTOMER.setTitle("Customer");
    }

    /** Active user. */
    public static final int STATUS_ACTIVE = 0;
    /** Blocked user. */
    public static final int STATUS_DISABLED = 1;
    /** User provided from external auth system. Is active, but shouldn't be edited in the current one. */
    public static final int STATUS_EXTERNAL = 2;

    private String login;
    private String password;
    private int status = STATUS_ACTIVE;
    // TODO: Rename to comment similar with others?.
    private String description = "";
    private Set<Integer> groupIds = new HashSet<>();
    private List<Integer> permsetIds = new ArrayList<>();
    private Set<Integer> queueIds = new HashSet<>();

    private String config = "";
    private ConfigMap configMap = new Preferences();

    private String personalization = "";
    private Preferences personalizationMap = new Preferences();

    public User() {}

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    /**
     * @return is the user admin with {@link #getId()} equals {@code 1}.
     */
    public boolean isAdmin() {
        return id == 1;
    }

    @Override
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getPassword() {
        if (Utils.isBlankString(password)) {
            return PswdUtil.EMPTY_PASSWORD;
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Group IDs active on the current date.
     * @return
     */
    public Set<Integer> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Set<Integer> groupIds) {
        if (groupIds != null) {
            this.groupIds = groupIds;
        }
    }

    public List<Integer> getPermsetIds() {
        return permsetIds;
    }

    public void setPermsetIds(List<Integer> permsetIds) {
        if (permsetIds != null) {
            this.permsetIds = permsetIds;
        }
    }

    public Set<Integer> getQueueIds() {
        return queueIds;
    }

    public void setQueueIds(Set<Integer> queueIds) {
        if (queueIds != null) {
            this.queueIds = queueIds;
        }
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        this.configMap = new Preferences(config);
    }

    @JsonIgnore
    public ConfigMap getConfigMap() {
        return configMap;
    }

    @JsonIgnore
    public String getPersonalization() {
        return personalization;
    }

    public void setPersonalization(String personalization) {
        this.personalization = personalization;
        this.personalizationMap = new Preferences(personalization);
    }

    @JsonIgnore
    public Preferences getPersonalizationMap() {
        return personalizationMap;
    }

    @Override
    public int compareTo(User o) {
        return title.compareTo(o.getTitle());
    }

    @Override
    public User clone() {
        var user = new User();

        user.setConfig(config);
        user.setDescription(description);
        user.setGroupIds(new HashSet<>(groupIds));
        user.setId(id);
        user.setLogin(login);
        user.setPassword(password);
        user.setPermsetIds(new ArrayList<>(permsetIds));
        user.setPersonalization(personalization);
        user.setQueueIds(new HashSet<>(queueIds));
        user.setStatus(status);
        user.setTitle(title);

        return user;
    }

    /**
     * List of users, fist is the current one.
     * After collected from {@link UserCache#getUserList()} users with intersected groups with the current one.
     * @return
     */
    @JsonIgnore
    public List<User> getUserListWithSameGroups() {
        var list = new ArrayList<User>(200);
        list.add(this);
        list.addAll(UserCache.getUserList().stream()
            .filter(u -> u.getId() != this.getId() && !CollectionUtils.intersection(this.getGroupIds(), u.getGroupIds()).isEmpty())
            .collect(Collectors.toList()));
        return list;
    }

    /**
     * Checks if {@code action} allowed in user permissions.
     * @param action semicolon separated action class and method names.
     * @return is the action allowed.
     */
    @Dynamic
    public boolean checkPerm(String action) {
        var node = PermissionNode.getPermissionNode(action);
        if (node != null && !node.getAction().equals(action))
            log.warn("Not primary action name '{}' was used for checking of '{}'", action, node.getAction());

        return UserCache.getPerm(id, action) != null;
    }
}
