package ru.bgcrm.model.param;

import java.math.BigDecimal;

public class ParameterListCountValue
{
	private BigDecimal count;
	private String comment;
	
	public ParameterListCountValue( BigDecimal paramDouble, String paramString )
	{
		this.count = paramDouble;
		this.comment = paramString;
	}
	
	public BigDecimal getCount()
	{
		return count;
	}
	public void setCount( BigDecimal paramDouble )
	{
		this.count = paramDouble;
	}
	public String getComment()
	{
		return comment;
	}
	public void setComment( String paramString )
	{
		this.comment = paramString;
	}	
}
