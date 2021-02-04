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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.distr.call.ExecuteSQL;
import ru.bgerp.l10n.Localization;
import ru.bgerp.l10n.Localizer;
import ru.bgerp.util.Log;

/**
 * Parent class for all the plugins.
 * @author Shamil Vakhitov
 */
public abstract class Plugin {
    private static final Log log = Log.getLog();

    protected static final String PATH_JS = "/js";
    protected static final String PATH_CSS = "/css";

    private final String id;
    /** Old way of plugin definition. XML document storing at most only endpoints. */
    private final Document document;
    private final Map<String, String> endpoints;

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

    /** Use {@link #getId()} */
    @Deprecated
    public String getName() {
        return getId();
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
    protected Map<String, String> loadEndpoints() {
        Map<String, String> endpoints = new HashMap<>(20);
        if (this.document != null) {
            for (Element endpoint : XMLUtils.selectElements(document, "/plugin/endpoint")) {
                endpoints.put(endpoint.getAttribute("id"), endpoint.getAttribute("file"));
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
     * Endpoints for connecting the plugin in JSP templates.
     * @return
     */
    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    /**
     * Initialization during server's start.
     * @param con
     * @throws Exception
     */
    public void init(Connection con) throws Exception {
        log.info("Plugin '%s', class '%s' init", getId(), getClass().getName());
        initDB(con);
    }

    private void initDB(Connection con) throws SQLException, IOException {
        var script = getClass().getResourceAsStream("db.sql");
        if (script != null) {
            log.info("%s applying db.sql", getId());
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
            License.getInstance().getPlugins().contains(getId()) :
            Utils.parseBoolean(defaultValue, false);
        return config.getBoolean(getId() + ":enable", defaultValueBool);
    }

    /**
     * Dependencies plugin IDs.
     * @return
     */
    public Set<String> getDependencies() {
        return Collections.emptySet();
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
        return Localization.getLocalizer(getId(), toLang);
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
     * Outdated path, used by plugin. Related to the application's root directory.
     * May be used for cleaning up of old files and directories.
     * TODO: For webapps is used separated logic.
     * @return
     */
    public Set<String> getUnusedPaths() {
        return Collections.emptySet();
    }
}
