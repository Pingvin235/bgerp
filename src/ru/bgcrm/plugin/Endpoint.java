package ru.bgcrm.plugin;

/**
 * Plugin's endpoints related constants and methods.
 * @author Shamil Vakhitov
 */
public class Endpoint {
    public static final String JS = "js";
    public static final String CSS = "css.jsp";
    public static final String USER_MENU_ITEMS = "user.menu.items.jsp";
    public static final String OPEN_JSP = "open.jsp";

    public static final String getPathPluginJS(String pluginId) {
        return Plugin.PATH_JS + "/pl." + pluginId + ".js";
    }

    public static final String getPathPluginCSS(String pluginId) {
        return Plugin.PATH_CSS + "/style." + pluginId + ".css.jsp";
    }
}