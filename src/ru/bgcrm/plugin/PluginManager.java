package ru.bgcrm.plugin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;
import org.reflections.Reflections;

import ru.bgcrm.util.Setup;

/**
 * Singleton plugin loader and storage.
 *
 * @author Shamil Vakhitov
 */
public class PluginManager {
    private static final Log log = Log.getLog();

    public static final String[] ERP_PACKAGES = { "org.bgerp", "ru.bgcrm" };

    private static PluginManager instance;

    public static void init() throws Exception {
        instance = new PluginManager();
        instance.initPlugins();
    }

    public static PluginManager getInstance() {
        return instance;
    }

    private final List<Plugin> fullSortedPluginList;
    private final Map<String, Plugin> fullPluginMap;
    private final List<Plugin> pluginList;
    private final Map<String, Plugin> pluginMap;

    private PluginManager() throws Exception {
        log.info("Plugins loading..");

        this.fullSortedPluginList = loadFullSortedPluginList();
        this.fullPluginMap = Collections.unmodifiableMap(
            fullSortedPluginList.stream().collect(Collectors.toMap(Plugin::getId, p -> p))
        );
        this.pluginList = loadPlugins();
        this.pluginMap = Collections.unmodifiableMap(
            pluginList.stream().collect(Collectors.toMap(Plugin::getId, p -> p))
        );
    }

    /**
     * Sorted plugin list. First kernel plugin, after the rest alphabetically sorted by ID.
     * @param pluginMap
     * @return
     */
    private List<Plugin> loadFullSortedPluginList() {
        List<Plugin> result = new ArrayList<>();

        var r = new Reflections((Object[]) ERP_PACKAGES);
        for (Class<? extends Plugin> pc : r.getSubTypesOf(Plugin.class)) {
            log.debug("Found plugin: {}", pc);
            try {
                Plugin instance = null;

                try {
                    instance = (Plugin) pc.getField("INSTANCE").get(null);
                } catch (NoSuchFieldException e) {
                    var constructor = pc.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    log.warn("No INSTANCE found in: {}", pc);

                    instance = constructor.newInstance();
                }

                result.add(instance);
            } catch (Exception e) {
                log.error("Error loading of plugin: " + pc, e);
            }
        }

        result.sort((p1, p2) -> {
            if (p1.isSystem() && !p2.isSystem())
                return -1;
            if (p2.isSystem() && !p1.isSystem())
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
        var enabledDefault = setup.get("plugin.enable.default", "0");
        List<Plugin> result =
            this.fullSortedPluginList.stream().filter(p -> p.isEnabled(setup, enabledDefault))
            .collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }

    /**
     * Runs {@link Plugin#init(Connection)} for enabled plugins.
     * @throws Exception
     */
    private void initPlugins() throws Exception {
        log.info("Running init() for enabled plugins.");
        for (Plugin p : pluginList) {
            try (Connection con = Setup.getSetup().getDBConnectionFromPool()) {
                p.init(con);
                con.commit();
            }
        }
    }

    /**
     * Complete list of all plugins. First kernel plugin, after the rest alphabetically sorted by ID.
     * @return
     */
    @Dynamic
    public List<Plugin> getFullSortedPluginList() {
        return fullSortedPluginList;
    }

    /**
     * List of enabled plugins.
     * @return
     */
    @Dynamic
    public List<Plugin> getPluginList() {
        return pluginList;
    }

    /**
     * List of not active plugins that can be activated.
     * @return
     */
    @Dynamic
    public List<Plugin> getInactivePluginList() {
        var activeIds = pluginList.stream().map(Plugin::getId).collect(Collectors.toSet());
        var result = fullSortedPluginList.stream()
            .filter(p -> !activeIds.contains(p.getId()) && !p.isSystem())
            .collect(Collectors.toList());
        return result;
    }

    /**
     * Map of enabled plugins, key {@link Plugin#getId()}.
     * @return
     */
    @Dynamic
    public Map<String, Plugin> getPluginMap() {
        return pluginMap;
    }

     /**
     * Complete map of all plugins, key {@link Plugin#getId()}.
     * @return
     */
    public Map<String, Plugin> getFullPluginMap() {
        return fullPluginMap;
    }
}