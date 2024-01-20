package org.bgerp.action.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionMapping;
import org.bgerp.action.BaseAction;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Action method invoker.
 *
 * @author Shamil Vakhitov
 */
public class Invoker {
    public static final Class<?>[] TYPES_CONSET_DYNFORM = { DynActionForm.class, ConnectionSet.class };
    public static final Class<?>[] TYPES_CON_DYNFORM = { DynActionForm.class, Connection.class };

    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CONSET_DYNFORM = { ActionMapping.class, DynActionForm.class,
            ConnectionSet.class };
    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CON_DYNFORM = { ActionMapping.class, DynActionForm.class,
            Connection.class };
    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CONSET_DYNFORM_SEVLETREQRESP = { ActionMapping.class, DynActionForm.class,
            HttpServletRequest.class, HttpServletResponse.class, ConnectionSet.class };
    @Deprecated
    private static final Class<?>[] TYPES_MAPPING_CON_DYNFORM_SEVLETREQRESP = { ActionMapping.class, DynActionForm.class,
            HttpServletRequest.class, HttpServletResponse.class, Connection.class };

    protected final Method method;

    private Invoker(Method method) {
        this.method = method;
        method.setAccessible(true);
        if (Modifier.isStatic(method.getModifiers()))
            throw new IllegalArgumentException("Action method can't be static");
    }

    public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response, ConnectionSet conSet)
                    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return method.invoke(action, actionForm, conSet);
    }


    private static class InvokerCon extends Invoker {
        public InvokerCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, actionForm, conSet.getConnection());
        }
    }

    private static class InvokerMapping extends Invoker {
        public InvokerMapping(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, conSet);
        }
    }

    private static class InvokerMappingCon extends Invoker {
        public InvokerMappingCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, conSet.getConnection());
        }
    }

    private static class InvokerWithRequest extends Invoker {
        public InvokerWithRequest(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, request, response, conSet);
        }
    }

    private static class InvokerWithRequestCon extends Invoker {
        public InvokerWithRequestCon(Method method) {
            super(method);
        }

        @Override
        public Object invoke(BaseAction action, ActionMapping mapping, DynActionForm actionForm, HttpServletRequest request,
                HttpServletResponse response, ConnectionSet conSet)
                        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(action, mapping, actionForm, request, response, conSet.getConnection());
        }
    }

    /**
     * Finds invoker method for a class using different signatures.
     * @param clazz the class.
     * @param method method name.
     * @param checkDeprecatedSignatures check deprecated method signatures.
     * @return not null instance.
     * @throws NoSuchMethodException invoker wasn't found.
     */
    public static final Invoker find(Class<?> clazz, String method, boolean checkDeprecatedSignatures) throws NoSuchMethodException {
        Invoker result = null;

        try {
            result = new Invoker(clazz.getDeclaredMethod(method, TYPES_CONSET_DYNFORM));
        } catch (Exception e) {}

        if (result == null) {
            try {
                result = new InvokerCon(clazz.getDeclaredMethod(method, TYPES_CON_DYNFORM));
            } catch (Exception e) {}
        }

        if (checkDeprecatedSignatures) {
            if (result == null) {
                try {
                    result = new InvokerMapping(clazz.getDeclaredMethod(method, TYPES_MAPPING_CONSET_DYNFORM));
                } catch (Exception e) {}
            }

            if (result == null) {
                try {
                    result = new InvokerMappingCon(clazz.getDeclaredMethod(method, TYPES_MAPPING_CON_DYNFORM));
                } catch (Exception e) {}
            }

            if (result == null) {
                try {
                    result = new InvokerWithRequest(clazz.getDeclaredMethod(method, TYPES_MAPPING_CONSET_DYNFORM_SEVLETREQRESP));
                } catch (Exception e) {}
            }

            if (result == null) {
                try {
                    result = new InvokerWithRequestCon(clazz.getDeclaredMethod(method, TYPES_MAPPING_CON_DYNFORM_SEVLETREQRESP));
                } catch (Exception e) {}
            }
        }

        if (result == null)
            throw new NoSuchMethodException(method);

        return result;
    }
}