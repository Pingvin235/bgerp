package ru.bgcrm.model.process.queue;

import org.bgerp.Interface;

import ru.bgcrm.model.Id;
import ru.bgcrm.util.ParameterMap;

public class Processor extends Id {
    private final String title;
    private final String iface;
    private final String className;
    private final String page;
    private final String pageUrl;
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
        pageUrl = config.get("page.url");
    }

    public String getResponseType() {
        return responseType;
    }

    public String getTitle() {
        return title;
    }

    /**
     * @return interface name from {@link Interface}.
     */
    public String getIface() {
        return iface;
    }

    /**
     * @return the class name for {@link Interface#USER} interface.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return JSP page with parameters.
     */
    public String getPage() {
        return page;
    }

    /**
     * @return action URL to be included.
     */
    public String getPageUrl() {
        return pageUrl;
    }

    public ParameterMap getConfigMap() {
        return configMap;
    }
}