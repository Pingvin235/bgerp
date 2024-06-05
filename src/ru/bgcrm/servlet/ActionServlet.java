package ru.bgcrm.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.BaseAction;
import org.apache.struts.config.ModuleConfig;
import org.bgerp.action.base.Actions;
import org.bgerp.util.Log;

import ru.bgcrm.plugin.PluginManager;

/**
 * Overwritten ActionServlet, loads actions for enabled plugins.
 *
 * @author Shamil Vakhitov
 */
public class ActionServlet extends org.apache.struts.action.ActionServlet {
    private static final Log log = Log.getLog();

    /**
     * Annotation for marking action classes.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Action {
        String path();
    }

    /**
     * Selects an action's path, declared in annotation {@link Action}.
     * @param clazz the action class.
     * @return string from the class annotation.
     * @throws IllegalArgumentException if no annotation defined.
     */
    public static String getActionPath(Class<? extends BaseAction> clazz) {
        var a = clazz.getDeclaredAnnotation(Action.class);
        if (a == null)
            throw new IllegalArgumentException();
        return a.path();
    }

    /**
     * Loads annotated actions.
     */
    @Override
    protected ModuleConfig initModuleConfig(String prefix, String paths) throws ServletException {
        var result = super.initModuleConfig(prefix, paths);

        Actions.init(PluginManager.getInstance().getPluginList());

        for (var ap : Actions.actionsByPath()) {
            String path = ap.getKey();
            String className = ap.getValue().getCanonicalName();

            var action = new ActionMapping();
            action.setPath(path);
            action.setType(className);
            action.setName("form");
            action.setScope("request");

            log.debug("Adding action class: {}, path: {}", className, path);

            result.addActionConfig(action);
        }

        return result;
    }
}