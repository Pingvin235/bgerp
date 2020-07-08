package ru.bgcrm.plugin;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.distr.call.ExecuteSQL;
import ru.bgerp.util.Log;

public abstract class Plugin {
    private static final Log log = Log.getLog();
    
    private final String name;
    private final Document document;
    private final Map<String, String> endpoints = new HashMap<>();

    protected Plugin(String pluginId) {
        name = pluginId;
        document = getXml("plugin.xml", XMLUtils.newDocument());
        for (Element endpoint : XMLUtils.selectElements(document, "/plugin/endpoint")) {
            String id = endpoint.getAttribute("id");
            String file = endpoint.getAttribute("file");
            endpoints.put(id, file);
        }
    }

    public Document getXml(String name, Document defaultValue) {
        InputStream is = getClass().getResourceAsStream(name);
        return is != null ? XMLUtils.parseDocument(is) : defaultValue;
    }

    public String getResourcePath(String name) {
        URL resource = getClass().getResource(name);
        return resource != null ? 
            getClass().getPackageName().replace('.', '/') + "/" + name :
            null;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    public Document getDocument() {
        return document;
    }

    public void init(Connection con) throws Exception {
        log.info("Plugin '%s', class '%s' init", getName(), getClass().getName());
        InputStream script = getClass().getResourceAsStream("db.sql");
        if (script != null) {
            log.info("%s applying db.sql", getName());
            new ExecuteSQL().call(con, IOUtils.toString(script, StandardCharsets.UTF_8));
        }
    }

}
