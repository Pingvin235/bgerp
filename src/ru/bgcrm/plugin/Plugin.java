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
import ru.bgerp.util.Log;

/**
 * Parent class for all the plugins.
 * @author Shamil Vakhitov
 */
public abstract class Plugin {
    private static final Log log = Log.getLog();

    private final String id;
    private final Document document;
    private final Map<String, String> endpoints = new HashMap<>();

    protected Plugin(String id) {
        this.id = id;
        this.document = getXml("plugin.xml", XMLUtils.newDocument());
        for (Element endpoint : XMLUtils.selectElements(document, "/plugin/endpoint")) {
            this.endpoints.put(endpoint.getAttribute("id"), endpoint.getAttribute("file"));
        }
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
     * Gets path of a file, placed int the plugin's package.
     * @param name name of the file.
     * @return
     */
    public String getResourcePath(String name) {
        URL resource = getClass().getResource(name);
        return resource != null ? getClass().getPackageName().replace('.', '/') + "/" + name : null;
    }

    /**
     * Endpoints from plugin's document {@link #getDocument()}.
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
     * @return
     */
    public Iterable<String> getUnusedPaths() {
        return Collections.emptyList();
    }
}
