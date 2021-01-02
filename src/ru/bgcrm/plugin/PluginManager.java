package ru.bgcrm.plugin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import ru.bgcrm.util.Setup;
import ru.bgerp.util.Log;

/**
 * @author Shamil Vakhitov
 */
public class PluginManager {
    private static final Log log = Log.getLog();

    public static final String KERNEL_PLUGIN_ID = org.bgerp.plugin.kernel.Plugin.ID;

    private static PluginManager instance;

    public static void init() throws Exception {
        instance = new PluginManager();
        instance.initPlugins();
    }

    public static PluginManager getInstance() {
        return instance;
    }

    private final List<Plugin> fullSortedPluginList;
    private final List<Plugin> pluginList;
    private final Map<String, Plugin> pluginMap;

    private PluginManager() throws Exception {
        log.info("Plugins loading..");

        this.fullSortedPluginList = loadFullSortedPluginList();
        this.pluginList = loadPlugins();
        this.pluginMap = Collections.unmodifiableMap(
            this.pluginList.stream().collect(Collectors.toMap(Plugin::getId, p -> p)) 
        );
    }

    /**
     * Sorted plugin list. First kernel plugin, after the rest alphabetically sorted by ID.
     * @param pluginMap
     * @return
     */
    private List<Plugin> loadFullSortedPluginList() {
        List<Plugin> result = new ArrayList<>();

        var r = new Reflections(new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forPackage("org.bgerp"))
            .addUrls(ClasspathHelper.forPackage("ru.bgerp"))
            .addUrls(ClasspathHelper.forPackage("ru.bgcrm"))
        );

        for (Class<? extends Plugin> pc : r.getSubTypesOf(Plugin.class)) {
            log.debug("Found plugin: %s", pc);
            try {
                var p = pc.getDeclaredConstructor().newInstance();
                result.add(p);
            } catch (Exception e) {
                log.error("Error loading of plugin: " + pc, e);
            }
        }

        result.sort((p1, p2) -> { 
            if (KERNEL_PLUGIN_ID.equals(p1.getId()))
                return -1;
            if (KERNEL_PLUGIN_ID.equals(p2.getId()))
                return 1;
            return p1.getId().compareTo(p2.getId());
        });

        return Collections.unmodifiableList(result);
    }

    /**
     * Searches plugin classes and loads enabled.
     * @return
     */
    private List<Plugin> loadPlugins() {
        var setup = Setup.getSetup();
        var enabledDefault = setup.get("plugin.enable.default", "1");
        List<Plugin> result = 
            this.fullSortedPluginList.stream().filter(p -> p.isEnabled(setup, enabledDefault))
            .collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }

    /**
     * On start init for enabled plugins.
     * @param pluginList
     */
    private void initPlugins() {
        log.info("Running init() for enabled plugins.");
        for (Plugin p : pluginList) {
            try (Connection con = Setup.getSetup().getDBConnectionFromPool()) {
                p.init(con);
                con.commit();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * Complete list of all plugins. First kernel plugin, after the rest alphabetically sorted by ID.
     * @return
     */
    public List<Plugin> getFullSortedPluginList() {
        return fullSortedPluginList;
    }

    /**
     * List of enabled plugins, used in JSP.
     * @return
     */
    public List<Plugin> getPluginList() {
        return pluginList;
    }

    /**
     * Map of enabled plugins, used in JSP.
     * @return
     */
    public Map<String, Plugin> getPluginMap() {
        return pluginMap;
    }
}