package ru.bgcrm.plugin;

/**
 * Plugin's endpoints related constants and methods.
 *
 * @author Shamil Vakhitov
 */
public class Endpoint {
    /** Well known endpoints. */
    public static final String JS = "js";
    public static final String CSS = "css.jsp";

    public static final String USER_MENU_ITEMS = "user.menu.items.jsp";
    public static final String USER_ADMIN_MENU_ITEMS = "user.admin.menu.items.jsp";
    public static final String USER_PROCESS_MENU_ITEMS = "user.process.menu.items.jsp";
    public static final String USER_PROCESS_TABS = "user.process.tabs.jsp";
    public static final String USER_PARAM_MENU_ITEMS = "user.param.menu.items.jsp";

    public static final String OPEN_JSP = "open.jsp";

    public static final String getPathPluginJS(String pluginId) {
        return Plugin.PATH_JS + "/pl." + pluginId + ".js";
    }

    public static final String getPathPluginCSS(String pluginId) {
        return Plugin.PATH_CSS + "/plugin/style." + pluginId + ".css.jsp";
    }
}
