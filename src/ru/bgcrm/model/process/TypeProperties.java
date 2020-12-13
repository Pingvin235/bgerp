package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.process.TransactionProperties.TransactionKey;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

/**
 * Process type properties.
 * 
 * @author Shamil Vakhitov
 */
public class TypeProperties {
    private static final Log log = Log.getLog();

    /** Initial status. */
    private int createStatus;
    /** Closing statuses. */
    private Set<Integer> closeStatusIds;
    /** Statuses. */
    private List<Integer> statusIds = new ArrayList<>();
    /** Parameters. */
    private List<Integer> parameterIds = new ArrayList<>();
    @Deprecated
    private String scriptName = "";
    /** String configuration. */
    private String config = "";
    /** Parsed configuration. */
    private ParameterMap configMap;
    /** Initial groups. */
    private Set<ProcessGroup> groups = new HashSet<>();
    /** Allowed groups. */
    private Set<ProcessGroup> allowedGroups = new HashSet<>();

    private LastModify lastModify = new LastModify();

    public Set<ProcessGroup> getGroups() {
        return groups;
    }

    public Set<ProcessGroup> getGroups(int roleId) {
        Set<ProcessGroup> groupSet = new HashSet<ProcessGroup>();

        for (ProcessGroup group : groups) {
            if (group.getRoleId() == roleId) {
                groupSet.add(group);
            }
        }

        return groupSet;
    }

    public Set<Integer> getGroupsSet() {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessGroup item : groups) {
            resultSet.add(item.getGroupId());
        }

        return resultSet;
    }

    public Set<Integer> getGroupsSet(int roleId) {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessGroup item : groups) {
            if (item.getRoleId() == roleId) {
                resultSet.add(item.getGroupId());
            }
        }

        return resultSet;
    }

    public void setGroups(Set<ProcessGroup> groups) {
        this.groups = groups;
    }

    public Set<Integer> getAllowedGroupsSet() {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessGroup item : allowedGroups) {
            resultSet.add(item.getGroupId());
        }

        return resultSet;
    }

    public Set<Integer> getAllowedGroupsSet(int roleId) {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessGroup item : allowedGroups) {
            if (item.getRoleId() == roleId) {
                resultSet.add(item.getGroupId());
            }
        }

        return resultSet;
    }

    // первый ключ - "с статуса", второй ключ - "на статус" 
    private Map<TransactionKey, TransactionProperties> transactionPropertiesMap = new HashMap<>();

    // мастер создания процесса через мобильный интерфейс
    private Wizard createWizard;

    public TypeProperties() {
    }

    public TypeProperties(String data, String config, LastModify lastModify) {
        transactionPropertiesMap.clear();

        this.lastModify = lastModify;

        Preferences setup = new Preferences(data);

        for (Map<String, String> transParam : setup.parseObjects("transaction.")) {
            var id = transParam.get("id");
            var key = new TransactionKey(id);
            var properties = new TransactionProperties(setup, "transaction." + id + ".");
            transactionPropertiesMap.put(key, properties);
        }
        scriptName = setup.get("script.name", "");
        createStatus = setup.getInt("create.status", 0);
        closeStatusIds = Utils.toIntegerSet(setup.get("close.status", ""));
        statusIds = Utils.toIntegerList(setup.get("status.ids", ""));
        parameterIds = Utils.toIntegerList(setup.get("param.ids", ""));
        allowedGroups = ProcessGroup.parseIdTitleSet(Utils.parseIdTitleList(setup.get("allowed.groups"), "0"));
        groups = ProcessGroup.parseIdTitleSet(Utils.parseIdTitleList(setup.get("create.groups"), "0"));

        this.config = config;

        try {
            Wizard wizard = new Wizard(this);
            if (wizard.getCreateStepList().size() > 0 || wizard.getStepList().size() > 0) {
                this.createWizard = wizard;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String serializeToData() {
        StringBuilder result = new StringBuilder();

        // действия при транзакциях
        for (Map.Entry<TransactionKey, TransactionProperties> me : transactionPropertiesMap.entrySet()) {
            TransactionKey key = me.getKey();
            TransactionProperties properties = me.getValue();

            String pref = "transaction." + key.fromStatus + "-" + key.toStatus + ".";
            properties.serializeToData(result, pref);
        }

        Utils.addSetupPair(result, "", "script.name", scriptName);
        Utils.addSetupPair(result, "", "create.status", String.valueOf(createStatus));
        Utils.addSetupPair(result, "", "close.status", Utils.toString(closeStatusIds));
        Utils.addSetupPair(result, "", "status.ids", Utils.toString(statusIds));
        Utils.addSetupPair(result, "", "param.ids", Utils.toString(parameterIds));
        Utils.addSetupPair(result, "", "create.groups", ProcessGroup.serialize(groups));
        Utils.addSetupPair(result, "", "allowed.groups", ProcessGroup.serialize(allowedGroups));

        return result.toString();
    }

    /**
     * Gets transaction properties.
     * 
     * @param fromStatus
     * @param toStatus
     * @return true, if transaction is enabled or do not defined.
     */
    public TransactionProperties getTransactionProperties(int fromStatus, int toStatus) {
        var key = new TransactionKey(fromStatus, toStatus);

        var result = transactionPropertiesMap.get(key);
        if (result == null)
            return TransactionProperties.ENABLED;

        return result;
    }

    public void setTransactionProperties(int fromStatus, int toStatus, boolean enabled) {
        var key = new TransactionKey(fromStatus, toStatus);

        var props = transactionPropertiesMap.get(key);
        if (props == null)
            props = new TransactionProperties(enabled);
        props.setEnable(enabled);

        transactionPropertiesMap.put(key, props);
    }

    public void clearTransactionProperties() {
        transactionPropertiesMap.clear();
    }

    @Deprecated
    public String getExecutor() {
        return configMap.get("setExecutor", "");
    }

    public Set<Integer> getAllowedStatusSet(int fromStatus) {
        Set<Integer> result = new HashSet<Integer>();

        for (Map.Entry<TransactionKey, TransactionProperties> me : transactionPropertiesMap.entrySet()) {
            TransactionKey key = me.getKey();
            TransactionProperties props = me.getValue();

            if (key.fromStatus == fromStatus && props.isEnable() && statusIds.contains(key.toStatus)) {
                result.add(key.toStatus);
            }
        }

        if (result.isEmpty()) { // Если список пустой, то мы проверяем пустая ли матрица, если так, то разрешаем переход на любой статус( пустая матрица == полная матрица )
            boolean emptyMatrix = transactionPropertiesMap.entrySet().stream()
                    .noneMatch(t -> statusIds.contains(t.getKey().toStatus) && statusIds.contains(t.getKey().fromStatus) && t.getValue().isEnable());
            if (emptyMatrix) {
                result.addAll(statusIds);
                result.remove(fromStatus); // вообще далее, в тех местах где используется, он будет снова добавлен. По этому вроде как можно и не удалять, но кто знает как будет в будущем использован(или скриптами), пусть содержание соответсвует названию.
            }
        }

        return result;
    }

    public List<Integer> getStatusIds() {
        return statusIds;
    }

    public void setStatusIds(List<Integer> statusIds) {
        this.statusIds = statusIds;
    }

    public List<Integer> getParameterIds() {
        return parameterIds;
    }

    public void setParameterIds(List<Integer> parameterIds) {
        this.parameterIds = parameterIds;
    }

    public int getCreateStatus() {
        return createStatus;
    }

    public void setCreateStatus(int createStatus) {
        this.createStatus = createStatus;
    }

    public Set<Integer> getCloseStatusIds() {
        return closeStatusIds;
    }

    public void setCloseStatusIds(Set<Integer> closeStatusIds) {
        this.closeStatusIds = closeStatusIds;
    }

    public String getScriptName() {
        return scriptName;
    }

    // Смотрит скрипт сначала в конфигурации а потом в поле 	
    public String getActualScriptName() {
        String listner = configMap.get("listenerClass");
        if (Utils.notBlankString(listner)) {
            return listner;
        }

        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public Set<ProcessGroup> getAllowedGroups() {
        return allowedGroups;
    }

    public Set<ProcessGroup> getAllowedGroups(int roleId) {
        Set<ProcessGroup> groupSet = new HashSet<ProcessGroup>();

        for (ProcessGroup group : allowedGroups) {
            if (group.getRoleId() == roleId) {
                groupSet.add(group);
            }
        }

        return groupSet;
    }

    public void setAllowedGroups(Set<ProcessGroup> allowedGroups) {
        this.allowedGroups = allowedGroups;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public ParameterMap getConfigMap() {
        if (configMap == null)
            configMap = new Preferences(config);
        return configMap;
    }
    
    public void setConfigMap(ParameterMap configMap) {
        this.configMap = configMap;
    }

    public LastModify getLastModify() {
        return lastModify;
    }

    public void setLastModify(LastModify lastModify) {
        this.lastModify = lastModify;
    }

    public Wizard getCreateWizard() {
        return createWizard;
    }

    public Set<Integer> getAllowedRoleSet() {
        Set<Integer> resultSet = new HashSet<Integer>();

        for (ProcessGroup group : allowedGroups) {
            if (!resultSet.contains(group.getRoleId())) {
                resultSet.add(group.getRoleId());
            }
        }

        return resultSet;
    }
}