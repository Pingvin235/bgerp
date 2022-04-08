package ru.bgcrm.cache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.util.Log;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserGroupDAO;
import ru.bgcrm.dao.user.UserPermsetDAO;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.SQLUtils;

public class UserCache extends Cache<UserCache> {
    private static final Log log = Log.getLog();

    private static CacheHolder<UserCache> holder = new CacheHolder<>(new UserCache());

    private static final ParameterMap EMPTY_PERMISSION = new Preferences();

    public static User getUser(int id) {
        return holder.getInstance().userMapById.get(id);
    }

    public static Map<Integer, User> getUserMap() {
        return holder.getInstance().userMapById;
    }

    /**
     * Finds user with status not {@link User#STATUS_DISABLED}.
     * @param login
     * @return
     */
    public static User getUser(String login) {
        return holder.getInstance().activeUserMapByLogin.get(login);
    }

    public static List<User> getUserList() {
        return holder.getInstance().userList;
    }

    public static Collection<User> getActiveUsers() {
        return holder.getInstance().activeUserMapByLogin.values();
    }

    public static Group getUserGroup(int groupId) {
        List<Group> groups = holder.getInstance().userGroupList;
        for (Group group : groups) {
            if (group.getId() == groupId) {
                return group;
            }
        }

        return null;
    }

    public static int getUserGroupChildCount(int groupId) {
        int result = 0;

        for (Group group : holder.getInstance().userGroupList) {
            if (group.getParentId() == groupId) {
                result++;
            }
        }

        return result;
    }

    /**
     * Called from JSP.
     * Gets user permission for action.
     * @param userId user ID.
     * @param action semicolon separated action class name and method, e.g. {@code org.bgerp.plugin.bil.billing.invoice.action.InvoiceAction:get}.
     * @return allowed permission with options or {@code null}.
     */
    public static ParameterMap getPerm(int userId, String action) {
        User user = getUser(userId);

        boolean dontCheckPermission = user.getConfigMap().getBoolean("dontCheckPermission", false);

        boolean permCheck = Setup.getSetup().getBoolean("user.permission.check", false);
        if (permCheck && !dontCheckPermission && user.getId() != 1) {
            Map<String, ParameterMap> userPerm = holder.getInstance().userPermMap.get(userId);
            if (userPerm != null) {
                // получение первичного имени акшена, на случай, если есть синонимы
                PermissionNode node = PermissionNode.getPermissionNode(action);
                if (node != null) {
                    if (node.isAllowAll()) {
                        return EMPTY_PERMISSION;
                    }

                    action = node.getAction();
                }

                ParameterMap map = userPerm.get(action);
                if (map != null) {
                    return map;
                }
            }
            return null;
        } else {
            return EMPTY_PERMISSION;
        }
    }

    public static List<User> getUserList(Set<Integer> groupIds) {
        List<User> result = new ArrayList<User>();

        for (User user : holder.getInstance().userList) {
            if (CollectionUtils.intersection(groupIds, user.getGroupIds()).size() > 0) {
                result.add(user);
            }
        }

        return result;
    }

    public static Set<Group> getUserGroupChildSet(int groupId) {
        Set<Group> resultSet = new HashSet<Group>();

        for (Group group : holder.getInstance().userGroupList) {
            if (group.getParentId() == groupId) {
                resultSet.add(group);
            }
        }

        return resultSet;
    }

    public static Set<Group> getUserGroupChildFullSet(int groupId) {
        Set<Group> resultSet = new HashSet<Group>();
        resultSet.addAll(getUserGroupChildSet(groupId));

        if (resultSet.size() > 0) {
            List<Group> groupList = new ArrayList<Group>(resultSet);

            for (int i = 0; i < groupList.size(); i++) {
                if (groupList.get(i).getChildCount() > 0) {
                    resultSet.addAll(getUserGroupChildFullSet(groupList.get(i).getId()));
                }
            }
        }

        return resultSet;
    }

    public static List<Group> getUserGroupList() {
        return holder.getInstance().userGroupList;
    }

    public static List<Group> getUserGroupFullTitledList() {
        return holder.getInstance().userGroupFullTitledList;
    }

    public static Map<Integer, Group> getUserGroupMap() {
        return holder.getInstance().userGroupMap;
    }

    public static Map<Integer, Group> getUserGroupFullTitledMap() {
        return holder.getInstance().userGroupFullTitledMap;
    }

    /**
     * @return alphabetically sorted list with all permission sets.
     */
    public static List<Permset> getUserPermsetList() {
        return holder.getInstance().userPermsetList;
    }

    /**
     * @return map with all use permission sets, key - ID
     */
    public static Map<Integer, Permset> getUserPermsetMap() {
        return holder.getInstance().userPermsetMap;
    }

    /**
     * @return list of root nodes for permission trees of enabled plugins.
     */
    public static List<PermissionNode> getPermTrees() {
        return holder.getInstance().allPermTrees;
    }

    public static void flush(Connection con) {
        holder.flush(con);
    }

    public static List<Group> getGroupPath(int id) {
        List<Group> result = new ArrayList<Group>();

        Group script = new Group();
        script.setParentId(id);

        while (script.getParentId() != 0) {
            script = holder.getInstance().userGroupMap.get(script.getParentId());
            result.add(0, script);
        }
        return result;
    }

    /**
     * "Возвращает полный путь к корневой группе в виде строки (например: Администратор -> Помощник -> Помощник помощника)"
     * @param id группы
     * @return Строка с полным путем к корневой группе, либо title группы, если нет родительской группы
     */
    public static String getUserGroupWithPath(Map<Integer, Group> groupMap, int id, boolean withId) {
        Group group = groupMap.get(id);
        String titleWithPath = group.getTitle();

        if (withId) {
            titleWithPath += " (" + group.getId() + ")";
        }

        while (group.getParentId() != 0) {
            int parentId = group.getParentId();

            group = groupMap.get(parentId);
            if (group == null) {
                log.warn("Not found parent group with id: " + parentId);
                break;
            }

            if (withId) {
                titleWithPath = group.getTitle() + " (" + group.getId() + ") " + " / " + titleWithPath;
            } else {
                titleWithPath = group.getTitle() + " / " + titleWithPath;
            }
        }

        return titleWithPath;
    }

    public static List<UserGroup> getUserGroupList(int id) {
        return holder.getInstance().userGroupListsMap.get(id) == null ? new ArrayList<UserGroup>()
                : holder.getInstance().userGroupListsMap.get(id);
    }

    public static List<UserGroup> getUserGroupList(int id, Date actualDate) {
        return getUserGroupList(id, -1, actualDate);
    }

    public static List<UserGroup> getUserGroupList(int id, int parentId, Date actualDate) {
        List<UserGroup> resultList = new ArrayList<UserGroup>();

        List<UserGroup> groupList = holder.getInstance().userGroupListsMap.get(id);
        if (groupList != null) {
            for (UserGroup item : groupList) {
                Group group = holder.getInstance().userGroupMap.get(item.getGroupId());
                if (group != null) {
                    if ((parentId < 0 || group.getParentId() == parentId) && (actualDate == null
                            || ((item.getDateFrom() != null && actualDate.compareTo(item.getDateFrom()) >= 0)
                                    && (item.getDateTo() == null || item.getDateTo().compareTo(actualDate) >= 0)))) {
                        resultList.add(item);
                    }
                }
            }
        }

        return resultList;
    }

    /*public static Map<Integer,List<UserGroup>> getUserGroupLinkList()
    {
        return holder.getInstance().userGroupLink;
    }*/

    // конец статической части

    private List<User> userList;
    private Map<Integer, User> userMapById;
    private Map<String, User> activeUserMapByLogin;

    private List<Permset> userPermsetList;
    private Map<Integer, Permset> userPermsetMap;

    private List<Group> userGroupList;
    private Map<Integer, Group> userGroupMap;
    private List<Group> userGroupFullTitledList;
    private Map<Integer, Group> userGroupFullTitledMap;

    private Map<Integer, Map<String, ParameterMap>> userPermMap;

    private List<PermissionNode> allPermTrees;

    private UserCache result;

    private Map<Integer, List<UserGroup>> userGroupListsMap;

    @SuppressWarnings("serial")
    @Override
    protected UserCache newInstance() {
        result = new UserCache();

        Setup setup = Setup.getSetup();

        Connection con = setup.getDBConnectionFromPool();
        try {
            UserDAO userDAO = new UserDAO(con);
            UserPermsetDAO permsetDAO = new UserPermsetDAO(con);
            UserGroupDAO groupDAO = new UserGroupDAO(con);

            result.userList = userDAO.getUserList();

            result.userMapById = new HashMap<Integer, User>() {
                @Override
                public User get(Object key) {
                    Integer id = (Integer) key;

                    User result = super.get(id);
                    if (result == null) {
                        result = new User();
                        result.setId(id);
                        result.setTitle("Не существует (" + id + ")");
                    }

                    return result;
                }
            };

            for (User user : result.userList) {
                result.userMapById.put(user.getId(), user);
            }

            result.activeUserMapByLogin = new TreeMap<String, User>();
            for (User user : result.userList) {
                if (user.getStatus() != User.STATUS_DISABLED) {
                    result.activeUserMapByLogin.put(user.getLogin(), user);
                }
            }

            // группы
            result.userGroupList = groupDAO.getGroupList();

            result.userGroupMap = new HashMap<Integer, Group>(result.userGroupList.size());
            for (Group group : result.userGroupList) {
                result.userGroupMap.put(group.getId(), group);
            }

            result.userGroupFullTitledList = new ArrayList<Group>();
            for (Group group : result.userGroupList) {
                Group fullTitled = group.clone();
                fullTitled.setTitle(UserCache.getUserGroupWithPath(result.userGroupMap, group.getId(), false));
                result.userGroupFullTitledList.add(fullTitled);
            }

            result.userGroupFullTitledMap = new HashMap<Integer, Group>(result.userGroupFullTitledList.size());
            for (Group group : result.userGroupFullTitledList) {
                result.userGroupFullTitledMap.put(group.getId(), group);
            }

            //привязки пользователей и групп
            result.userGroupListsMap = userDAO.getAllUserGroups();

            // наборы прав
            result.userPermsetList = permsetDAO.getPermsetList();

            result.userPermsetMap = new HashMap<Integer, Permset>(result.userPermsetList.size());
            for (Permset permset : result.userPermsetList) {
                result.userPermsetMap.put(permset.getId(), permset);
            }

            // сбор свойств пользователей из групп и наборов прав
            Map<Integer, List<Integer>> allUserPermsetIds = userDAO.getAllUserPermsetIds();
            Map<Integer, Set<Integer>> allUserQueueIds = userDAO.getAllUserQueueIds();

            Map<Integer, List<Integer>> allGroupPermsetIds = groupDAO.getAllGroupPermsetIds();
            Map<Integer, Set<Integer>> allGroupQueueIds = groupDAO.getAllGroupQueueIds();

            Map<Integer, Map<String, ParameterMap>> allUserPermById = userDAO.getAllUserPerm();
            Map<Integer, Map<String, ParameterMap>> allPermsetPermById = permsetDAO.getAllPermsets();

            result.userPermMap = new HashMap<Integer, Map<String, ParameterMap>>();

            for (User user : result.userList) {
                Map<String, ParameterMap> perm = new HashMap<String, ParameterMap>();
                result.userPermMap.put(user.getId(), perm);

                user.setPermsetIds(allUserPermsetIds.get(user.getId()));
                user.setQueueIds(allUserQueueIds.get(user.getId()));

                List<UserGroup> ugList = result.userGroupListsMap.get(user.getId());
                if (ugList != null) {
                    user.setGroupIds(getActualUserGroupIdSet(new Date(), ugList));
                }

                // действующие группы пользователя
                List<Group> userGroupList = new ArrayList<Group>();
                for (Group group : result.userGroupList) {
                    if (user.getGroupIds().contains(group.getId())) {
                        userGroupList.add(group);
                    }
                }

                // действующие наборы прав пользователя
                List<Integer> userPermsetIds = new ArrayList<Integer>();
                // сначала собираются все наборы групп в порядке алфавита, установленных у пользователя
                // наборы в каждой группе установлены в определённом порядке
                for (Group group : userGroupList) {
                    List<Integer> groupPermsetIds = allGroupPermsetIds.get(group.getId());
                    if (groupPermsetIds != null) {
                        userPermsetIds.addAll(groupPermsetIds);
                    }
                }
                // далее - наборы из пользователя в порядке установки
                userPermsetIds.addAll(user.getPermsetIds());

                // склеенная конфигурация наборов прав
                StringBuilder fullUserConfig = new StringBuilder(500);

                // сбор прав, ролей и конфигураций из действующих наборов пользователя
                for (Integer permsetId : userPermsetIds) {
                    Map<String, ParameterMap> permsetPermMap = allPermsetPermById.get(permsetId);
                    if (permsetPermMap != null) {
                        perm.putAll(permsetPermMap);
                    }

                    // склеивание ролей и конфигураций
                    Permset permset = result.userPermsetMap.get(permsetId);
                    if (permset != null) {
                        user.setRoles(user.getRoles() + " " + permset.getRoles());

                        // пока просто склеивание конфигов полностью а нужно сделать склеивание на уровне параметров,
                        // чтобы города объединялись
                        fullUserConfig.append(permset.getConfig());
                        fullUserConfig.append("\n");
                    } else {
                        log.warn("Permset not exists: " + permsetId + ", setted for user.");
                    }
                }

                // склеенная конфигурация групп
                StringBuilder groupConfig = new StringBuilder(500);

                for (Group group : userGroupList) {
                    addGroupConfig(group.getId(), groupConfig);
                }

                user.setConfig(fullUserConfig.toString() + groupConfig.toString() + user.getConfig());

                // персональные права
                Map<String, ParameterMap> personalPermMap = allUserPermById.get(user.getId());
                if (personalPermMap != null) {
                    perm.putAll(personalPermMap);
                }

                // очереди процессов из групп
                for (Integer groupId : user.getGroupIds()) {
                    Set<Integer> groupQueueIds = allGroupQueueIds.get(groupId);
                    if (groupQueueIds != null) {
                        user.getQueueIds().addAll(groupQueueIds);
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("User id: " + user.getId() + "; login: " + user.getLogin() + "; roles: " + user.getRoles()
                            + "; queueIds: " + user.getQueueIds() + "; config: \n" + user.getConfig() + "; groups: "
                            + userGroupList + "; permsetIds: " + userPermsetIds);
                }
            }

            result.allPermTrees = PermissionNode.getPermissionTrees();

            User user = new User();
            user.setId(User.USER_CUSTOMER_ID);
            user.setTitle("Customer");
            result.userMapById.put(user.getId(), user);

            user = new User();
            user.setId(User.USER_SYSTEM_ID);
            user.setTitle("System");
            user.setLogin(setup.get("user.system.login"));
            user.setPassword(setup.get("user.system.pswd"));
            result.userMapById.put(user.getId(), user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
        }

        return result;
    }

    private Set<Integer> getActualUserGroupIdSet(Date actualDate, List<UserGroup> ugList) {
        Set<Integer> activeGroupSet = new HashSet<Integer>();

        for (UserGroup ug : ugList) {
            if (TimeUtils.dateInRange(actualDate, ug.getDateFrom(), ug.getDateTo())) {
                activeGroupSet.add(ug.getGroupId());
            }
        }

        return activeGroupSet;
    }

    private void addGroupConfig(Integer groupId, StringBuilder config) {
        Group group = result.userGroupMap.get(groupId);

        if (group != null) {
            Integer parentId = group.getParentId();
            if (parentId > 0) {
                addGroupConfig(parentId, config);
            }

            config.append(group.getConfig());
            config.append("\n");
        }
    }

}
