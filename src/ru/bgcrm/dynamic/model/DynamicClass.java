package ru.bgcrm.dynamic.model;

import javax.xml.bind.annotation.XmlAttribute;

public class DynamicClass
{
	private String className;
	private long lastModified;
	//private boolean needsRecompile;
	
	private boolean deprecated;
	private String description;
	
	public DynamicClass()
    {}
	
	public DynamicClass( String className, long lastModified )
	{
		this.className = className;
		this.lastModified = lastModified;
	}
	
	@XmlAttribute
	public String getClassName()
    {
    	return className;
    }
	
	public void setClassName( String className )
    {
    	this.className = className;
    }
	
	@XmlAttribute
	public long getLastModified()
    {
    	return lastModified;
    }
	
	public void setLastModified( long lastModified )
    {
    	this.lastModified = lastModified;
    }

	/*public void setNeedsRecompile( boolean needsRecompile )
    {
	    this.needsRecompile = needsRecompile;
    }

	@XmlAttribute
	public boolean isNeedsRecompile()
    {
	    return needsRecompile;
    }*/

	public boolean isDeprecated()
    {
    	return deprecated;
    }

	public void setDeprecated( boolean deprecated )
    {
    	this.deprecated = deprecated;
    }

	public String getDescription()
    {
    	return description;
    }

	public void setDescription( String description )
    {
    	this.description = description;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( obj == null ) return false;
		if( getClass() != obj.getClass() ) return false;
		DynamicClass other = (DynamicClass)obj;
		if( className == null )
		{
			if( other.className != null ) return false;
		}
		else if( !className.equals( other.className ) ) return false;
		return true;
	}

	@Override
    public String toString()
    {
		return className + (deprecated ? " (deprecated)" : "");
    }
}
