package ru.bgcrm.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.util.Log;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class Config implements LastModifySupport {
    private static final Log log = Log.getLog();

    public static final String INCLUDE_PREFIX = "include.";

    private int id = -1;
    @Deprecated
    private int userId = -1;
    private boolean active = false;
    @Deprecated
    private Date date;
    /** Raw data. */
    private String data;
    private String title;
    /** Parent configuration ID. */
    private int parentId;
    /** List of included configurations. */
    private List<Config> includedList;
    /** Parsed key-value pairs. */
    private ParameterMap valueMap = new Preferences();
    private LastModify lastModify = new LastModify();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Deprecated
    public int getUserId() {
        return userId;
    }

    @Deprecated
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Main active configuration.
     * @return
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Deprecated
    public Date getDate() {
        return date;
    }

    @Deprecated
    public void setDate(Date date) {
        this.date = date;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        this.valueMap = new Preferences(data);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    /**
     * Adds included configuration.
     * @param included
     */
    public void addIncluded(Config included) {
        if (includedList == null)
            includedList = new ArrayList<>();
        includedList.add(included);
        includedList.sort((c1, c2) -> c1.title.compareTo(c2.title));
    }

    /**
     * Included configurations, sorted by title.
     * @return
     */
    public List<Config> getIncludedList() {
        return includedList;
    }

    public ParameterMap getValueMap() {
        return valueMap;
    }

    public LastModify getLastModify() {
        return lastModify;
    }

    public void setLastModify(LastModify lastModify) {
        this.lastModify = lastModify;
    }

    /**
     * Comma separated list of enabled plugins titles.
     * @return
     */
    public String getEnabledPluginsTitles() {
        var config = new Preferences(data);

        return
            PluginManager.getInstance().getFullSortedPluginList().stream()
            .filter(p -> !p.isSystem() && p.isEnabled(config, "0"))
            .map(Plugin::getTitle)
            .collect(Collectors.joining(", "));

        /* var result = new StringBuilder(100);

        for (String key : new Preferences(data).keySet()) {
            if (!key.endsWith(Plugin.ENABLE_KEY_SUFFIX))
                continue;

            String pluginId = key.substring(0, key.length() - Plugin.ENABLE_KEY_SUFFIX.length());
            var plugin = PluginManager.getInstance().getFullPluginMap().get(pluginId);
            if (plugin == null) {
                log.warn("Not existing plugin '{}' is presented in config {}", pluginId, this.id);
                continue;
            }

            if (plugin.isEnabled(config, defaultValue))
        }

        return result.toString(); */
    }

    /**
     * Selects includes from global configuration like: include.<id>=1.
     *
     * @param configDao configuration selector.
     * @param data configuration raw data, containing includes.
     * @param validate выброс исключения при не найденной конфигурации.
     * @throws BGMessageException validation
     * @throws SQLException
     */
    public static Iterable<ParameterMap> getIncludes(ConfigDAO configDao, ParameterMap data, boolean validate) throws BGMessageException, SQLException {
        List<ParameterMap> result = new ArrayList<>();

        for (Map.Entry<String, String> me : data.sub(Config.INCLUDE_PREFIX).entrySet()) {
            int configId = Utils.parseInt(me.getKey());

            boolean load = Utils.parseBoolean(me.getValue());
            if (load && configId > 0) {
                Config config = configDao.getGlobalConfig(configId);
                if (config == null) {
                    String message = "Not found included config: " + configId;
                    if (validate) throw new BGMessageException(message);
                    log.error(message);
                    continue;
                }
                result.add(new Preferences(config.getData()));
            }
        }

        return result;
    }
}