package org.bgerp.app.servlet.jsp.tag;

import javax.servlet.jsp.JspException;

public class SetTag extends org.apache.taglibs.standard.tag.rt.core.SetTag {

    @Override
    public int doEndTag() throws JspException {
        if (bodyContent != null && "null".equals(bodyContent.getString()))
            setBodyContent(null);
        return super.doEndTag();
    }

}