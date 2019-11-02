package ru.bgcrm.model.param;


/*
import ru.bgcrm.util.Utils;

public class ParameterListValue
{
    private int value = -1;
    private String comment;
    
    public ParameterListValue()
    {}
    
    public ParameterListValue( String valueWithComment )
    {
    	this.value = Utils.parseInt( StringUtils.substringBefore( valueWithComment, ":" ) );
    	this.comment = Utils.maskNull( StringUtils.substringAfter( valueWithComment, ":" ) );
    }

    public int getValue()
    {
        return value;
    }

    public void setValue( int value )
    {
        this.value = value;
    }

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	@Override
	public int hashCode()
	{
		return value;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( obj == null ) return false;
		if( getClass() != obj.getClass() ) return false;
		ParameterListValue other = (ParameterListValue)obj;
		if( value != other.value ) return false;
		return true;
	}
}*/