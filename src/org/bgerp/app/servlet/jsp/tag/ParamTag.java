package org.bgerp.app.servlet.jsp.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * u:param inside of u:newInstance
 */
public class ParamTag extends BodyTagSupport {
    protected Object value;

    public ParamTag() {
        super();
        init();
    }

    private void init() {
        value = null;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    // simply send our name and value to our appropriate ancestor
    public int doEndTag() throws JspException {
        NewInstanceTag t = (NewInstanceTag) findAncestorWithClass(this, NewInstanceTag.class);
        if (t == null)
            throw new JspTagException("PARAM_OUTSIDE_PARENT");

        // send the parameter to the appropriate ancestor
        Object value = this.value;
        if (value == null) {
            if (bodyContent == null || bodyContent.getString() == null)
                value = "";
            else
                value = bodyContent.getString().trim();
        }

        t.addParameter(value);

        return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        init();
    }
}
