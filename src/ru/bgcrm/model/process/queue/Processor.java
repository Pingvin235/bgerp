package ru.bgcrm.model.process.queue;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.servlet.Interface;
import org.bgerp.app.servlet.jsp.GetJsp;
import org.bgerp.model.base.Id;
import org.bgerp.util.Log;

import ru.bgcrm.event.ProcessMarkedActionEvent;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Processor may be defined in two ways:
 * <li> name of an extending class, used normally in{@link Interface#USER} interface;
 * <li> page URL pointing to an action.
 *
 * @author Shamil Vakhitov
 */
public class Processor extends Id implements GetJsp {
    private static final Log log = Log.getLog();

    private final String title;
    private final String iface;
    private final String className;
    private final String pageUrl;
    private final boolean htmlReport;
    private final ConfigMap configMap;

    public Processor(int id, ConfigMap configMap) {
        this.id = id;
        this.configMap = configMap;
        title = configMap.get("title");
        iface = configMap.get("iface", Interface.USER);
        className = configMap.get("class");
        htmlReport = "file".equals(configMap.get("responseType"));
        pageUrl = configMap.get("page.url");
        String page = configMap.get("page");
        if (Utils.notBlankString(page))
            log.warnd("Configuration key 'page' is no longer needed in processor definition: {}", page);
    }

    public ConfigMap getConfigMap() {
        return configMap;
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
     * @return the class name for a class-based processor.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return is HTML report generated by a class-based processor.
     */
    public boolean isHtmlReport() {
        return htmlReport;
    }

    /**
     * @return JSP page with parameters for a class-based processor.
     */
    @Override
    public String getJsp() {
        return null;
    }

    /**
     * Processing event for a class-based processor.
     * @param e the event with selected processes.
     * @param conSet DB connections.
     * @throws Exception
     */
    public void process(ProcessMarkedActionEvent e, ConnectionSet conSet) throws Exception {}

    /**
     * @return action URL to be included.
     */
    public String getPageUrl() {
        return pageUrl;
    }
}