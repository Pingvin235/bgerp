package ru.bgcrm.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.BaseAction;
import org.apache.struts.config.ModuleConfig;
import org.reflections.Reflections;

import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgerp.util.Log;

/**
 * Overwritten ActionServlet, loads struts-config.xml also for enabled plugins.
 * 
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
            if (path == null)
                continue;
            paths.append(",").append(path);
        }

        this.config = paths.toString();

        log.info("Action config paths: %s", this.config);

        super.init();
    }

    /**
     * Annotation for marking action classes.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Action {
        String path();
    }

    /**
     * Loads annotated actions.
     */
    @Override
    protected ModuleConfig initModuleConfig(String prefix, String paths) throws ServletException {
        var result = super.initModuleConfig(prefix, paths);

        for (var p : PluginManager.getInstance().getPluginList()) {
            var r = new Reflections(p.getActionPackages());
            for (Class<? extends BaseAction> ac : r.getSubTypesOf(BaseAction.class)) {
                var a = ac.getDeclaredAnnotation(Action.class);
                if (a == null) continue;

                var action = new ActionMapping();
                action.setPath(a.path());
                action.setParameter("action");
                action.setType(ac.getCanonicalName());
                action.setName("form");
                action.setScope("request");

                log.debug("Add action for plugin: %s, class: %s, path: %s", p.getId(), ac.getCanonicalName(), a.path());

                result.addActionConfig(action);
            }
        } 

        return result;
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
}