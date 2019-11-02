package ru.bgcrm.plugin;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.util.XMLUtils;

public abstract class Plugin {
    private final String name;
    private final Document document;
    private Map<String, String> endpoints = new HashMap<String, String>();

    // для каждого плагина должен быть определён public конструктор с параметром Document
    protected Plugin(Document doc, String pluginId) {
        document = doc;
        name = pluginId;

        for (Element endpoint : XMLUtils.selectElements(doc, "/plugin/endpoint")) {
            String id = endpoint.getAttribute("id");
            String file = endpoint.getAttribute("file");
            //String entity = endpoint.getAttribute("entity");
            endpoints.put(id, file);
        }
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
}
