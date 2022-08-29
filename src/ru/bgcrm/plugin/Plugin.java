package ru.bgcrm.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bgerp.dao.Cleaner;
import org.bgerp.l10n.Localization;
import org.bgerp.l10n.Localizer;
import org.bgerp.plugin.msg.email.MessageTypeEmail;
import org.bgerp.util.Log;
import org.bgerp.util.lic.AppLicense;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.distr.call.ExecuteSQL;

/**
 * Parent class for all the plugins.
 *
 * @author Shamil Vakhitov
 */
public abstract class Plugin {
    private static final String ENABLE_KEY_SUFFIX = ":enable";

    private static final Log log = Log.getLog();

    protected static final String PATH_JS = "/js";
    protected static final String PATH_LIB = "/lib";
    protected static final String PATH_CSS = "/css";

    protected static final String PATH_JSP_ADMIN_PLUGIN = BaseAction.PATH_JSP_ADMIN + "/plugin";
    protected static final String PATH_JSP_USER_PLUGIN = BaseAction.PATH_JSP_USER + "/plugin";
    protected static final String PATH_JSP_OPEN_PLUGIN = BaseAction.PATH_JSP_OPEN + "/plugin";

    private final String id;
    /** Old way of plugin definition. XML document storing at most only endpoints. */
    private final Document document;
    private final Map<String, List<String>> endpoints;

    protected Plugin(String id) {
        this.id = id;
        this.document = getXml("plugin.xml", XMLUtils.newDocument());
        this.endpoints = loadEndpoints();
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
     * Plugin's XML document from 'plugin.xml'.
     * @return parsed document object from or empty document if there is no such file.
     */
    public Document getDocument() {
        return document;
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
     * Default implementation, loads endpoints from the XML {@link #document}.
     * Deprecated way, for backward compatibility only.
     * @return
     */
    protected Map<String, List<String>> loadEndpoints() {
        Map<String, List<String>> endpoints = new HashMap<>(20);
        if (this.document != null) {
            for (Element endpoint : XMLUtils.selectElements(document, "/plugin/endpoint")) {
                endpoints.put(endpoint.getAttribute("id"), Collections.singletonList(endpoint.getAttribute("file")));
            }
        }
        return Collections.unmodifiableMap(endpoints);
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
    public boolean isEnabled(ParameterMap config, String defaultValue) {
        var defaultValueBool =
            "lic".equals(defaultValue) ?
            AppLicense.instance().getPlugins().contains(getId()) :
            Utils.parseBoolean(defaultValue, false);
        return config.getBoolean(getId() + ENABLE_KEY_SUFFIX, defaultValueBool);
    }

    /**
     * List of supported by plugin languages, main is the first.
     * For any of supported languages must be presented translated documentation and localized UI.
     * @return default return single list of {@link Lang#RU}.
     */
    public List<Lang> getLanguages() {
        return List.of(Lang.RU);
    }

    /**
     * Localizer to target language.
     * @param toLang
     * @return
     */
    public Localizer getLocalizer(String toLang) {
        return Localization.getLocalizer(toLang, getId());
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
