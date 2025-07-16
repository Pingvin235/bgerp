package ru.bgcrm.plugin.bgbilling;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.UserCache;
import org.bgerp.util.sql.pool.ConnectionPool;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.directory.Directory;
import ru.bgcrm.plugin.bgbilling.proto.model.UserInfo;

/**
 * Данные для подсоединения к биллингу.
 */
public class DBInfo {
    private final ConfigMap setup;
    private final String id;
    private final String url;
    private final URL serverUrl;
    private final String title;
    private String version;
    private final ConnectionPool connectionPool;

    private final BiMap<Integer, Integer> billingUserIdUserIdBiMap = HashBiMap.create(100);

    private Set<String> pluginSet;
    private ConfigMap guiConfigValues;

    private final Map<String, Directory<?>> directories = new ConcurrentHashMap<>();

    public DBInfo(ConfigMap config) {
        this.id = config.get("id");
        this.setup = config;
        this.url = config.get("url");
        try {
            serverUrl = new URI(url).toURL();
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new BGException(ex);
        }
        title = config.get("title");
        version = config.get("version", "");

        this.connectionPool = new ConnectionPool("bgbilling-" + getId(), config);
    }

    public ConfigMap getSetup() {
        return setup;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public URL getServerUrl() {
        return serverUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        version = value;
    }

    public int versionCompare(String withVersion) {
        return new BigDecimal(this.version).compareTo(new BigDecimal(withVersion));
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    // код текстового параметра с кодом контрагента
    public int getCustomerIdParam() {
        return setup.getInt("customerIdParam", 0);
    }

    public Set<String> getPluginSet() {
        return pluginSet;
    }

    public void setPluginSet(Set<String> pluginSet) {
        this.pluginSet = pluginSet;
    }

    public ConfigMap getGuiConfigValues() {
        return guiConfigValues;
    }

    public void setGuiConfigValues(ConfigMap config) {
        guiConfigValues = config;
    }

    public TransferData getTransferData() {
        return new TransferData(this);
    }

    public String getCopyParamMapping() {
        return setup.get("copyParamMapping");
    }

    public DBInfo loadUsers(User requestUser) {
        if (billingUserIdUserIdBiMap.isEmpty()) {
            synchronized (billingUserIdUserIdBiMap) {
                Map<String, Integer> billingLoginUserId = new DirectoryDAO(requestUser, this).getUserList().stream()
                        .collect(Collectors.toMap(UserInfo::getLogin, UserInfo::getId));

                for (User user : UserCache.getUserMap().values()) {
                    Integer billingUserId = user.getConfigMap().getInt("bgbilling:userId." + id, 0);
                    if (billingUserId > 0) {
                        billingUserIdUserIdBiMap.put(billingUserId, user.getId());
                    } else {
                        billingUserId = billingLoginUserId.get(user.getLogin());
                        if (billingUserId != null)
                            billingUserIdUserIdBiMap.put(billingUserId, user.getId());
                    }
                }
            }
        }
        return this;
    }

    public int getBillingUserId(int userId) {
        return billingUserIdUserIdBiMap.inverse().get(userId);
    }

    public int getUserId(int billingUserId) {
        Integer value = billingUserIdUserIdBiMap.get(billingUserId);
        return value != null ? value : -1;
    }

    @SuppressWarnings("unchecked")
    public <D extends Directory<?>> D directory(Class<D> clazz) {
        // not used yet
        final int moduleId = 0;
        final String key = clazz.getName() + ":" + moduleId;
        return (D) directories.computeIfAbsent(key, unused -> {
            try {
                return clazz.getDeclaredConstructor(DBInfo.class, int.class).newInstance(this, moduleId);
            } catch (Exception e) {
                throw new BGException(e);
            }
        });
    }
}