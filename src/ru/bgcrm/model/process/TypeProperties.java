package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.model.process.ProcessGroups;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.process.TransactionProperties.TransactionKey;
import ru.bgcrm.util.Utils;

/**
 * Process type properties.
 *
 * @author Shamil Vakhitov
 */
public class TypeProperties {
    private static final Log log = Log.getLog();

    /** Initial status. */
    private int createStatusId;
    /** Closing statuses. */
    private Set<Integer> closeStatusIds;
    /** Statuses. */
    private List<Integer> statusIds = new ArrayList<>();
    /** Parameters. */
    private List<Integer> parameterIds = new ArrayList<>();
    /** String configuration. */
    private String config = "";
    /** Parsed configuration. */
    private ConfigMap configMap;
    /** Initial groups. */
    private ProcessGroups groups = new ProcessGroups();
    /** Allowed groups. */
    private ProcessGroups allowedGroups = new ProcessGroups();

    private LastModify lastModify = new LastModify();

    // первый ключ - "с статуса", второй ключ - "на статус"
    private Map<TransactionKey, TransactionProperties> transactionPropertiesMap = new HashMap<>();

    // мастер создания процесса через мобильный интерфейс
    private Wizard wizard;

    public TypeProperties() {
    }

    public TypeProperties(String data, String config, LastModify lastModify) {
        transactionPropertiesMap.clear();

        this.lastModify = lastModify;

        Preferences configMap = new Preferences(data);

        for (String key : configMap.subKeyed("transaction.").keySet()) {
            var properties = new TransactionProperties(configMap, "transaction." + key + ".");
            transactionPropertiesMap.put(new TransactionKey(key), properties);
        }

        createStatusId = configMap.getInt("create.status", 0);
        closeStatusIds = Utils.toIntegerSet(configMap.get("close.status", ""));
        statusIds = Utils.toIntegerList(configMap.get("status.ids", ""));
        parameterIds = Utils.toIntegerList(configMap.get("param.ids", ""));
        allowedGroups = ProcessGroups.of(Utils.parseIdTitleList(configMap.get("allowed.groups"), "0"));
        groups = ProcessGroups.of(Utils.parseIdTitleList(configMap.get("create.groups"), "0"));

        this.config = config;

        try {
            Wizard wizard = new Wizard(this);
            if (wizard.getCreateStepList().size() > 0 || wizard.getStepList().size() > 0) {
                this.wizard = wizard;
            }
        } catch (Exception e) {
            log.error(e);
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

        Utils.addSetupPair(result, "", "create.status", String.valueOf(createStatusId));
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

    public Set<Integer> getAllowedStatusSet(int fromStatus) {
        Set<Integer> result = new HashSet<>();

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

    public int getCreateStatusId() {
        return createStatusId;
    }

    public void setCreateStatusId(int createStatus) {
        this.createStatusId = createStatus;
    }

    public Set<Integer> getCloseStatusIds() {
        return closeStatusIds;
    }

    public void setCloseStatusIds(Set<Integer> closeStatusIds) {
        this.closeStatusIds = closeStatusIds;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public ConfigMap getConfigMap() {
        if (configMap == null)
            configMap = new Preferences(config);
        return configMap;
    }

    public void setConfigMap(ConfigMap configMap) {
        this.configMap = configMap;
    }

    /**
     * @return initial process groups.
     */
    public ProcessGroups getGroups() {
        return groups;
    }

    public ProcessGroups getGroups(int roleId) {
        Set<ProcessGroup> processGroups = groups.stream()
            .filter(pg -> pg.roleId == roleId)
            .collect(Collectors.toSet());

        return new ProcessGroups(processGroups);
    }

    public void setGroups(ProcessGroups groups) {
        this.groups = groups;
    }

    @Dynamic
    public ProcessGroups getAllowedGroups() {
        return allowedGroups;
    }

    @Dynamic
    public ProcessGroups getAllowedGroups(int roleId) {
        Set<ProcessGroup> processGroups = allowedGroups.stream()
            .filter(pg -> pg.roleId == roleId)
            .collect(Collectors.toSet());

        return new ProcessGroups(processGroups);
    }

    public void setAllowedGroups(ProcessGroups allowedGroups) {
        this.allowedGroups = allowedGroups;
    }

    public LastModify getLastModify() {
        return lastModify;
    }

    public void setLastModify(LastModify lastModify) {
        this.lastModify = lastModify;
    }

    public Wizard getWizard() {
        return wizard;
    }
}