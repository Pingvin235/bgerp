package ru.bgcrm.plugin.bgbilling.proto.model.script;

public class ContractScript
{
	private String comment;
	private String period;
	private String dateFrom;
	private String dateTo;
	private Integer typeId;
	private String title;
	private Integer id;

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public String getPeriod()
	{
		return period;
	}

	public void setPeriod( String period )
	{
		this.period = period;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId( Integer id )
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getDateFrom()
	{
		return dateFrom;
	}

	public void setDateFrom( String dateFrom )
	{
		this.dateFrom = dateFrom;
	}

	public String getDateTo()
	{
		return dateTo;
	}

	public void setDateTo( String dateTo )
	{
		this.dateTo = dateTo;
	}

	public Integer getTypeId()
	{
		return typeId;
	}

	public void setTypeId( Integer typeId )
	{
		this.typeId = typeId;
	}
}
