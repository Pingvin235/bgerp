package ru.bgcrm.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionMapping;
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
        /**
         * @return the action ID is path, not class name
         */
        boolean pathId() default false;
    }

    /**
     * Loads annotated actions.
     */
    @Override
    protected ModuleConfig initModuleConfig(String prefix, String paths) throws ServletException {
        var result = super.initModuleConfig(prefix, paths);

        Actions.init(PluginManager.getInstance().getPluginList());

        for (var a : Actions.actions()) {
            var action = new ActionMapping();
            action.setPath(a.getPath());
            action.setType(a.getType());
            action.setName("form");
            action.setScope("request");

            log.debug("Adding action class: {}, path: {}", a.getType(), a.getPath());

            result.addActionConfig(action);
        }

        return result;
    }
}