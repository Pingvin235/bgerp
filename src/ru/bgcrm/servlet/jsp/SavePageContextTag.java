package ru.bgcrm.servlet.jsp;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import ru.bgcrm.util.Utils;

public class SavePageContextTag
	extends TagSupport
{
	private Set<String> export = Collections.emptySet();
	private Map<String, Object> savedAttributes;
	
	public int doStartTag()
		throws JspException
	{
		savedAttributes = new HashMap<String, Object>();
		
		Enumeration<String> attributeNames = pageContext.getAttributeNamesInScope( PageContext.PAGE_SCOPE );
		while( attributeNames.hasMoreElements() )
		{
			String name = attributeNames.nextElement();
			savedAttributes.put( name, pageContext.getAttribute( name, PageContext.PAGE_SCOPE ) );
		}
		
		return EVAL_BODY_INCLUDE;
	}

	public int doEndTag()
	{
		for( String name : Collections.list( pageContext.getAttributeNamesInScope( PageContext.PAGE_SCOPE ) ) )
		{
			if( export.contains( name ) )
			{
				savedAttributes.put( name, pageContext.getAttribute( name, PageContext.PAGE_SCOPE ) );
			}			
			pageContext.removeAttribute( name, PageContext.PAGE_SCOPE );
		}
		
		for( Map.Entry<String, Object> me : savedAttributes.entrySet() )
		{
			pageContext.setAttribute( me.getKey(), me.getValue(), PageContext.PAGE_SCOPE );
		}
		
		return EVAL_PAGE;
	}
	
	public void setExport( String value )
	{
		export = Utils.toSet( value );
	}
}