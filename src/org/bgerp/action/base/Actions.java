package org.bgerp.action.base;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.servlet.ActionServlet;

/**
 * Action's registry
 *
 * @author Shamil Vakhitov
 */
public class Actions {
    /** Action classes by ID */
    private static final Map<String, Action> ACTIONS_BY_ID = new ConcurrentHashMap<>();
    /** Action classes by class */
    private static final Map<Class<? extends BaseAction>, Action> ACTIONS_BY_CLASS = new ConcurrentHashMap<>();

    public static void init(Iterable<Plugin> plugins) {
        ACTIONS_BY_ID.clear();
        ACTIONS_BY_CLASS.clear();

        for (var p : plugins) {
            var r = new Reflections(p.getActionPackages());
            for (Class<? extends BaseAction> ac : r.getSubTypesOf(BaseAction.class)) {
                var a = ac.getDeclaredAnnotation(ActionServlet.Action.class);
                if (a == null) {
                    // different types of base actions
                    continue;
                }

                var action = new Action(a, ac);
                ACTIONS_BY_ID.put(action.getId(), action);
                ACTIONS_BY_CLASS.put(ac, action);
            }
        }
    }

    /**
     * Gets an action by its ID
     * @param id the ID
     * @return the action or {@code null}
     */
    public static Action getById(String id) {
        return ACTIONS_BY_ID.get(id);
    }

    /**
     * Gets an action by its class
     * @param clazz the class
     * @return the action or {@code null}
     */
    public static Action getByClass(Class<? extends BaseAction> clazz) {
        return ACTIONS_BY_CLASS.get(clazz);
    }

    /**
     * All the actions
     * @return collection with all registered actions
     */
    public static Collection<Action> actions() {
        return ACTIONS_BY_ID.values();
    }

    /**
     * Action, a class with methods, called by HTTP
     */
    public static class Action {
        private final String path;
        private final String type;
        private final Class<? extends BaseAction> typeClass;
        private final String id;

        private Action(ActionServlet.Action action, Class<? extends BaseAction> clazz) {
            path = action.path();
            type = clazz.getCanonicalName();
            typeClass = clazz;
            id = action.pathId() ? path : type;
        }

        /**
         * @return URI path without {@code .do} ending, e.g. {@code /user/process}
         */
        public String getPath() {
            return path;
        }

        /**
         * @return action's class full name with package
         */
        public String getType() {
            return type;
        }

        /**
         * @return action's class
         */
        public Class<? extends BaseAction> getTypeClass() {
            return typeClass;
        }

        /**
         * @return action's path or class name, depending on {@link ActionServlet.Action#pathId()}
         */
        public String getId() {
            return id;
        }
    }
}
