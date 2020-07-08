package ru.bgcrm.servlet;

import javax.servlet.ServletException;

import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgerp.util.Log;

/**
 * Overwritten ActionServlet, loads struts-config.xml also for enabled plugins.
 * @author Shamil Vakhitov
 */
public class ActionServlet extends org.apache.struts.action.ActionServlet {
    private static final Log log = Log.getLog();

    private static final String FILE_NAME = "struts-config.xml";

    @Override
    public void init() throws ServletException {
        StringBuilder paths = new StringBuilder("/WEB-INF/" + FILE_NAME);

        for (Plugin p : PluginManager.getInstance().getPluginList()) {
            String path = p.getResourcePath(FILE_NAME);
            if (path == null) continue;
            paths
                .append(",")
                .append(path);
        }

        this.config = paths.toString();

        log.info("Actions config: %s", this.config);

        super.init();
    }
}