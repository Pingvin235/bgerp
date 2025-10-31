package org.bgerp.app.servlet.jsp.tag;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.bgerp.app.cfg.bean.Bean;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

public class NewInstanceTag extends BodyTagSupport {
    private static final Log log = Log.getLog();

    protected String context;

    // class name
    private String clazz;
    private String var;
    private List<Object> params;

    public NewInstanceTag() {
        super();
        init();
    }

    private void init() {
        clazz = null;
        var = null;
        params = null;
        context = null;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
    }

    public void addParameter(Object value) {
        params.add(value);
    }

    public int doStartTag() throws JspException {
        params = new ArrayList<>();
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        try {
            Class<?> clazz = Bean.getClass(this.clazz);
            for (Constructor<?> constr : clazz.getConstructors()) {
                if (constr.getParameterTypes().length == params.size()) {
                    Object[] convertedTypes = convertObjectTypes(params, constr.getParameterTypes());
                    if (convertedTypes != null) {
                        pageContext.setAttribute(var, constr.newInstance(convertedTypes));
                        log.debug("Created instance using: {}", convertedTypes);
                    }
                }
            }
        } catch (Exception e) {
            throw new JspException(e.getMessage(), e);
        }

        log.warnd("Deprecated JSP tag '<u:newInstance' was called. Use functions 'u:newInstance0'..'u:newInstance3' instead.");

        return EVAL_PAGE;
    }

    /**
     * Преобразует типы параметров конструктора, либо возвращает null.
     * @param params оригинальный массив параметров.
     * @param constructorParams типы параметров конструктора.
     * @return
     */
    public static Object[] convertObjectTypes(List<Object> params, Class<?>[] constructorParams) {
        Object[] result = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            Class<?> paramClass = params.get(i).getClass();
            if (constructorParams[i].isAssignableFrom(paramClass))
                result[i] = param;
            else if (paramClass.equals(String.class)) {
                if (constructorParams[i].equals(int.class) || constructorParams[i].equals(Integer.class))
                    result[i] = Utils.parseInt((String) param);
                else if (constructorParams[i].equals(long.class) || constructorParams[i].equals(Long.class))
                    result[i] = Utils.parseLong((String) param);
                else
                    return null;
            }
            else
                return null;
        }
        return result;
    }

    public void release() {
        init();
    }
}
