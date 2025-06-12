package ru.bgcrm.dao.expression;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;

/**
 * Extended JEXL context, providing creation of static class contexts by full name.
 * <pre>
 *  var = org.bgerp.SomeClass;
 *  var.someStaticMethod();
 * </pre>
 *
 * @author Shamil Vakhitov
 */
public class Context extends MapContext implements JexlContext.NamespaceResolver {
    @Override
    public boolean has(String name) {
        try {
            return super.has(name) || Class.forName(name) != null;
        } catch (ClassNotFoundException xnf) {
            return false;
        }
    }

    @Override
    public Object get(String name) {
        try {
            Object found = super.get(name);
            if (found == null && !super.has(name)) {
                found = Class.forName(name);
            }
            return found;
        } catch (ClassNotFoundException xnf) {
            return null;
        }
    }

    /**
     * Resolves only top-level namespace
     * @param name the namespace name, can be only {@code null}
     * @return the context object under key {@code null}
     * @throws UnsupportedOperationException if not {@code null} passed as name
     */
    @Override
    public Object resolveNamespace(String name) {
        if (name == null) {
            Object ns = get(null);
            if (ns == null)
                return null;
            return ns;
        }
        throw new UnsupportedOperationException("Only top-level namespace is supported, not: " + name);
    }
}
