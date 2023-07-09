package org.bgerp.app.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.custom.Custom;
import org.bgerp.util.Log;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.PluginManager;

public class Bean {
    private static final Log log = Log.getLog();

    private static volatile Map<String, Class<?>> beanClasses = Map.of();
    private static volatile Map<String, Class<?>> beanClassesOldNames = Map.of();

    static {
        loadBeanClasses();
    }

    /**
     * Reloads statically loaded and {@link Custom} {@link Bean} annotated classes.
     */
    public static final void loadBeanClasses() {
        log.debug("loadBeanClasses");

        var beanClasses = new HashMap<String, Class<?>>(100);
        var beanClassesOldNames = new HashMap<String, Class<?>>(200);

        var r = classes();
        for (var clazz : r.getTypesAnnotatedWith(org.bgerp.app.bean.annotation.Bean.class, false)) {
            String simpleName = clazz.getSimpleName();

            log.debug("Loading class: {}", clazz);

            beanClasses.put(clazz.getName(), clazz);

            var beanClass = beanClasses.get(simpleName);
            if (beanClass != null)
                log.error("Bean class simple name {} conflicts to an existing {}", clazz.getName(), beanClass.getName());
            else
                beanClasses.put(simpleName, clazz);

            var an = clazz.getDeclaredAnnotation(org.bgerp.app.bean.annotation.Bean.class);
            if (an != null)
                for (String name : an.oldClasses()) {
                    beanClassesOldNames.put(name, clazz);

                    simpleName = StringUtils.substringAfterLast(name, ".");
                    beanClass = beanClassesOldNames.get(simpleName);
                    if (beanClass != null)
                        log.debug("Bean class OLD simple name {} conflicts to an existing {}", simpleName, beanClass.getName());
                    else
                        beanClassesOldNames.put(simpleName, clazz);
                }
        }

        Bean.beanClasses = Collections.unmodifiableMap(beanClasses);
        Bean.beanClassesOldNames = Collections.unmodifiableMap(beanClassesOldNames);
    }

    /**
     * @return reflection object, configured for statically loaded from {@link PluginManager#ERP_PACKAGES} and {@link Custom} classes.
     */
    public static Reflections classes() {
        var builder = new ConfigurationBuilder().forPackages(PluginManager.ERP_PACKAGES);

        var customClassLoader = Custom.INSTANCE.getClassLoader();
        if (customClassLoader != null) {
            log.debug("Adding custom classloader");
            builder
                .addClassLoaders(customClassLoader)
                .forPackage(Custom.PACKAGE, customClassLoader);
        }

        return new Reflections(builder);
    }

    /**
     * Provides class by a simple {@link Bean} of a full class name.
     * Both {@link Custom} and normal classes are checked.
     * @param name the simple {@link Bean} of the full class name.
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> getClass(String name) throws ClassNotFoundException {
        Class<?> result = beanClasses.get(name);

        if (result == null) {
            result = beanClassesOldNames.get(name);
            if (result != null)
                log.warn("Bean class was found by an old name: {}", name);
        }

        if (result == null) {
            var customClassLoader = Custom.INSTANCE.getClassLoader();
            if (customClassLoader != null)
                result = Class.forName(name, true, customClassLoader);
            else
                result = Class.forName(name);
        }

        return result;
    }

    /**
     * Creates an object of a given class, loaded with {@link Bean#getClass(String)}.
     * @param className the full class name or a simple {@link Bean} name.
     * @param args optional constructor arguments.
     * @return created object instance.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static final <T> T newInstance(String name) throws BGException, ClassNotFoundException {
        try {
            Class<T> clazz = (Class<T>) getClass(name);
            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
            return null;
        } catch (ClassNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BGException(ex);
        }
    }
}
