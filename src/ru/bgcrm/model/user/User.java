package ru.bgcrm.model.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.UserAccount;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class User extends IdTitle implements Comparable<User>, Cloneable, UserAccount {
    public static final String OBJECT_TYPE = "user";

    public static final int USER_SYSTEM_ID = 0;
    public static final int USER_CUSTOMER_ID = -1;

    public static final int STATUS_ENABLE = 0;
    public static final int STATUS_DISABLE = 1;

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

    private String login;
    private String password;
    private int status = STATUS_ENABLE;
    // возможно, не нужен, т.к. есть параметр типа EMail
    private String email = "";
    private String roles = "";
    private String config = "";
    private String personalization = "";
    // TODO: Везде это называется comment.
    private String description = "";
    private ParameterMap configMap = new Preferences();
    private Preferences personalizationMap = new Preferences();
    private List<Integer> permsetIds = new ArrayList<Integer>();
    private Set<Integer> groupIds = new HashSet<Integer>();
    private Set<Integer> queueIds = new HashSet<Integer>();

    // идентификаторы, тут вроде как планировались номера телефонов логина
    // пока не используется
    private List<String> ids = new ArrayList<String>();

    public User() {
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Deprecated
    public void setEmail(String email) {
        this.email = email;
    }

    @Deprecated
    public String getEmail() {
        return email;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
        this.configMap = new Preferences(config);
    }

    public ParameterMap getConfigMap() {
        return configMap;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public List<Integer> getPermsetIds() {
        return permsetIds;
    }

    public void setPermsetIds(List<Integer> permsetIds) {
        if (permsetIds != null) {
            this.permsetIds = permsetIds;
        }
    }

    /**
     * Возвращает группы, активные на _текущий_ момент времени с учётом периодов.
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

    @Override
    public int compareTo(User o) {
        return title.compareTo(o.getTitle());
    }

    @Deprecated
    public List<String> getIds() {
        return ids;
    }

    @Deprecated
    public void setIds(List<String> phoneNumbers) {
        this.ids = phoneNumbers;
    }

    public Set<Integer> getQueueIds() {
        return queueIds;
    }

    public void setQueueIds(Set<Integer> queueIds) {
        if (queueIds != null) {
            this.queueIds = queueIds;
        }
    }

    public void addContextVariablesToConfig() {
        StringBuilder config = new StringBuilder(this.config);

        config.append("\nctxUserId=");
        config.append(String.valueOf(id));
        config.append("\nctxUserGroupIds=");
        config.append(Utils.toString(groupIds));
        config.append("\nctxUserPermsetIds=");
        config.append(Utils.toString(permsetIds));

        setConfig(config.toString());
    }

    public String getPersonalization() {
        return personalization;
    }

    public void setPersonalization(String personalization) {
        this.personalization = personalization;
        this.personalizationMap = new Preferences(personalization);
    }

    public Preferences getPersonalizationMap() {
        return personalizationMap;
    }

    public User clone() {
        User user = new User();

        user.setConfig(config);
        user.setDescription(description);
        user.setEmail(email);
        user.setGroupIds(new HashSet<Integer>(groupIds));
        user.setId(id);
        user.setIds(new ArrayList<String>(ids));
        user.setLogin(login);
        user.setPassword(password);
        user.setPermsetIds(new ArrayList<Integer>(permsetIds));
        user.setPersonalization(personalization);
        user.setQueueIds(new HashSet<Integer>(queueIds));
        user.setRoles(roles);
        user.setStatus(status);
        user.setTitle(title);

        return user;
    }
}
