package ru.bgcrm.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.dist.inst.call.ExecuteSQL;
import org.bgerp.app.dist.lic.AppLicense;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.dao.Cleaner;
import org.bgerp.plugin.msg.email.message.MessageTypeEmail;
import org.bgerp.util.Log;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;

import ru.bgcrm.util.Utils;

/**
 * Parent class for all the plugins.
 *
 * @author Shamil Vakhitov
 */
public abstract class Plugin {
    private static final Log log = Log.getLog();

    private static final String ENABLE_KEY_SUFFIX = ":enable";

    protected static final String PATH_JS = "/js";
    protected static final String PATH_LIB = "/lib";
    protected static final String PATH_CSS = "/css";

    protected static final String PATH_JSP_ADMIN_PLUGIN = BaseAction.PATH_JSP_ADMIN + "/plugin";
    protected static final String PATH_JSP_USER_PLUGIN = BaseAction.PATH_JSP_USER + "/plugin";
    protected static final String PATH_JSP_OPEN_PLUGIN = BaseAction.PATH_JSP_OPEN + "/plugin";

    private final String id;
    private final Map<String, List<String>> endpoints;
    private final Localization localization;

    protected Plugin(String id) {
        this.id = id;
        this.endpoints = loadEndpoints();
        this.localization = Localization.getLocalization(this);
    }

    /**
     * Plugin's ID.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Human readable plugin title.
     * @return
     */
    public String getTitle() {
        return id.substring(0, 1).toUpperCase() + id.substring(1);
    }

    /**
     * Prefix 'Plugin ' plus {@link #getTitle()}.
     * @return
     */
    public final String getTitleWithPrefix() {
        return "Plugin " + getTitle();
    }

    /**
     * System plugins are always loaded, enabled and not show in the list.
     * @return
     */
    public boolean isSystem() {
        return false;
    }

    /**
     * XML document from the plugin's package.
     * @param name
     * @param defaultValue
     * @return
     */
    public Document getXml(String name, Document defaultValue) {
        InputStream is = getClass().getResourceAsStream(name);
        return is != null ? XMLUtils.parseDocument(is) : defaultValue;
    }

    /**
     * Plugin endpoints
     * @return
     */
    protected Map<String, List<String>> loadEndpoints() {
        return Map.of();
    }

    /**
     * Gets path of a file, placed int the plugin's package.
     * @param name name of the file.
     * @return
     */
    public String getResourcePath(String name) {
        URL resource = getClass().getResource(name);
        return resource != null ? getClass().getPackageName().replace('.', '/') + "/" + name : null;
    }

    /**
     * Packages for searching annotated actions.
     * @return
     */
    public Set<String> getActionPackages() {
        return Set.of(this.getClass().getPackageName());
    }

    /**
     * Dependencies plugin IDs.
     * @return
     */
    public Set<String> getDependencies() {
        return Collections.emptySet();
    }

    /**
     * Endpoints for connecting the plugin in JSP templates.
     * @return
     */
    public final List<String> getEndpoints(String key) {
        return endpoints.get(key);
    }

    /**
     * Message type class, supported by the plugin.
     * @param name name of the type, must start from the plugin ID.
     * @return
     */
    public Class<? extends MessageTypeEmail> getMessageTypeClass(String name) {
        return null;
    }

    /**
     * Initialization during server's start.
     * @param con
     * @throws Exception
     */
    public void init(Connection con) throws Exception {
        log.info("Plugin '{}', class '{}' init", getId(), getClass().getName());
        initDB(con);
    }

    private void initDB(Connection con) throws SQLException, IOException {
        var script = getClass().getResourceAsStream("db.sql");
        if (script != null) {
            log.info("{} applying db.sql", getId());
            new ExecuteSQL().call(con, IOUtils.toString(script, StandardCharsets.UTF_8));
        }
    }

    /**
     * If the plugin enabled.
     * @param config configuration, where the plugin can be explicitly enabled.
     * @param defaultValue default value if not explicitly enabled: '1' - enabled, 'lic' - if presented in license.
     * @return true or false.
     */
    public boolean isEnabled(ConfigMap config, String defaultValue) {
        var defaultValueBool =
            "lic".equals(defaultValue) ?
            AppLicense.instance().getPlugins().contains(getId()) :
            Utils.parseBoolean(defaultValue, false);
        return config.getBoolean(getId() + ENABLE_KEY_SUFFIX, defaultValueBool);
    }

    /**
     * @return the plugin's localization from l10n.xml if exists, or {@code null}
     */
    public Localization getLocalization() {
        return localization;
    }

    /**
     * @return the plugin's only localization for {@link Localization#getLang()} language.
     */
    public Localizer getLocalizer() {
        return new Localizer(Localization.getLang(), getLocalization());
    }

    /**
     * Used DB tables.
     * @return
     */
    public Set<Table> getTables() {
        return Collections.emptySet();
    }

    /**
     * Plugin's entities.
     * @return
     */
    public Set<String> getObjectTypes() {
        return Collections.emptySet();
    }

    /**
     * DB cleaner.
     * @return
     */
    public Cleaner getCleaner() {
        return null;
    }
}
