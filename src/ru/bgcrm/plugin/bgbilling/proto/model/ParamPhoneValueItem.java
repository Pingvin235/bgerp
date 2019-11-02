package ru.bgcrm.plugin.bgbilling.proto.model;

import ru.bgcrm.model.param.ParameterPhoneValueItem;

@Deprecated
public class ParamPhoneValueItem
	extends ParameterPhoneValueItem
{
	/*private String phone;
	private String format;
	private String comment;
	private int flags = 0;

	public String getPhone()
	{
		return phone;
	}

	public void setPhone( String phone )
	{
		this.phone = phone;
	}

	public String getFormat()
	{
		return format;
	}

	public void setFormat( String format )
	{
		this.format = format;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public int getFlags()
	{
		return flags;
	}

	public void setFlags( int flags )
	{
		this.flags = flags;
	}
	
	public static final String toString( List<ParamPhoneValueItem> list )
	{
		StringBuilder result = new StringBuilder( 100 );
		
		for( ParamPhoneValueItem item : list )
		{
			Utils.addSeparated( result, "; ", item.getPhone() + " [" + item.getComment() + "]" );
		}
		
		return result.toString();
	}*/
}
