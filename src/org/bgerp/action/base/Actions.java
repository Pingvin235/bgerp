package org.bgerp.action.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.servlet.ActionServlet;

public class Actions {
    /** Action classes by path */
    private static final Map<String, Class<? extends BaseAction>> ACTIONS_BY_PATH = new ConcurrentHashMap<>();
    /** Action classes by class names */
    private static final Map<String, Class<? extends BaseAction>> ACTIONS_BY_CLASS = new ConcurrentHashMap<>();

    public static void init(Iterable<Plugin> plugins) {
        ACTIONS_BY_PATH.clear();
        ACTIONS_BY_CLASS.clear();

        for (var p : plugins) {
            var r = new Reflections(p.getActionPackages());
            for (Class<? extends BaseAction> ac : r.getSubTypesOf(BaseAction.class)) {
                var a = ac.getDeclaredAnnotation(ActionServlet.Action.class);
                if (a == null) {
                    // different types of base actions
                    continue;
                }

                ACTIONS_BY_PATH.put(a.path(), ac);
                ACTIONS_BY_CLASS.put(ac.getCanonicalName(), ac);
            }
        }
    }

    /**
     * Searches action by its class name
     * @param identifier the class name
     * @return found action class or {@code null}
     */
    public static Class<? extends BaseAction> get(String identifier) {
        /* var result = ACTIONS_BY_PATH.get(identifier);
        if (result == null)
            result = ACTIONS_BY_CLASS.get(identifier);
        return result; */
        return ACTIONS_BY_CLASS.get(identifier);
    }

    public static Iterable<Map.Entry<String, Class<? extends BaseAction>>> actionsByPath() {
        return ACTIONS_BY_PATH.entrySet();
    }
}
