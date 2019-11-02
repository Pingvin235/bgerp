package ru.bgcrm.servlet.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ParamTag
	extends BodyTagSupport
{
	protected Object value;

	public ParamTag() {
		super();
		init();
	}

	private void init()
	{
		value = null;
	}
	
	public void setValue( Object value )
	{
		this.value = value;
	}

	// simply send our name and value to our appropriate ancestor
	public int doEndTag()
		throws JspException
	{
		NewInstanceTag t = (NewInstanceTag)findAncestorWithClass( this, NewInstanceTag.class );
		if( t == null ) throw new JspTagException("PARAM_OUTSIDE_PARENT");

		// send the parameter to the appropriate ancestor
		Object value = this.value;
		if( value == null )
		{
			if( bodyContent == null || bodyContent.getString() == null ) value = "";
			else value = bodyContent.getString().trim();
		}
		
		t.addParameter( value );
		
		/*if( encode )
		{
			// FIXME: revert to java.net.URLEncoder.encode(s, enc) once
			// we have a dependency on J2SE 1.4+.
			String enc = pageContext.getResponse().getCharacterEncoding();
			parent.addParameter( Util.URLEncode( name, enc ), Util.URLEncode( value, enc ) );
		}
		else
		{
			parent.addParameter( name, value );
		}*/
		
		return EVAL_PAGE;
	}

	// Releases any resources we may have (or inherit)
	public void release()
	{
		init();
	}
}
