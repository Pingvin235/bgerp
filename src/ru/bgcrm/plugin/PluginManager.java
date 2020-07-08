package ru.bgcrm.plugin;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import ru.bgcrm.util.Setup;
import ru.bgerp.util.Log;

public class PluginManager {
    private static final Log log = Log.getLog();

    private static PluginManager instance;

    public static void init() throws Exception {
        instance = new PluginManager();
    }

    public static PluginManager getInstance() {
        return instance;
    }

    private final List<Plugin> pluginList;
    private final Map<String, Plugin> pluginMap;

    private PluginManager() throws Exception {
        log.info("Plugins loading..");

        var r = new Reflections(new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forPackage("org.bgerp"))
            .addUrls(ClasspathHelper.forPackage("ru.bgerp"))
            .addUrls(ClasspathHelper.forPackage("ru.bgcrm"))
        );

        var setup = Setup.getSetup();
        var enableDefault = setup.getBoolean("plugin.enable.default", true);

        Map<String, Plugin> pluginMap = new HashMap<>();

        for (Class<? extends Plugin> pc : r.getSubTypesOf(Plugin.class)) {
            log.debug("Found plugin: %s", pc);
            try {
                var p = pc.getDeclaredConstructor().newInstance();
                var name = p.getName();
                // all the plugins are enabled by default
                if (setup.getBoolean(name + ":enable", enableDefault))
                    pluginMap.put(name, p);
            } catch (Exception e) {
                log.error("Error loading of plugin: " + pc, e);
            }
        }

        this.pluginMap = Collections.unmodifiableMap(pluginMap);

        // name sorted enabled plugins
        List<Plugin> pluginList = pluginMap.values().stream()
            .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
            .collect(Collectors.toList());

        log.info("Running init() for enabled plugins.");
        for (Plugin p : pluginList) {
            try (Connection con = Setup.getSetup().getDBConnectionFromPool()) {
                p.init(con);
                con.commit();
            } catch (Exception e) {
                log.error(e);
            }
        }

        this.pluginList = Collections.unmodifiableList(pluginList);
    }

    public List<Plugin> getPluginList() {
        return pluginList;
    }

    public Map<String, Plugin> getPluginMap() {
        return pluginMap;
    }
}