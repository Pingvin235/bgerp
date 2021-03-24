package ru.bgcrm.model.process.queue;

import org.bgerp.Interface;

import ru.bgcrm.model.Id;
import ru.bgcrm.util.ParameterMap;

public class Processor extends Id {
    private final String title;
    private final String iface;
    private final String className;
    private final String page;
    private final String responseType;
    private final ParameterMap configMap;

    public Processor(int id, ParameterMap config) {
        this.id = id;
        this.configMap = config;

        title = config.get("title");
        iface = config.get("iface", Interface.USER);
        className = config.get("class");
        page = config.get("page");
        responseType = config.get("responseType");
    }

    public String getResponseType() {
        return responseType;
    }

    public String getTitle() {
        return title;
    }

    public String getIface() {
        return iface;
    }

    public String getClassName() {
        return className;
    }

    public String getPage() {
        return page;
    }

    public ParameterMap getConfigMap() {
        return configMap;
    }
}