package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

public class DialUpError
{
	private String id;
	private String recordId;
	private String cid;
	private String date;
	private String contract;
	private String login;
	private String nas;
	private String error;

	public DialUpError()
	{

	}

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getRecordId()
	{
		return recordId;
	}

	public void setRecordId( String recordId )
	{
		this.recordId = recordId;
	}

	public String getCid()
	{
		return cid;
	}

	public void setCid( String cid )
	{
		this.cid = cid;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate( String date )
	{
		this.date = date;
	}

	public String getContract()
	{
		return contract;
	}

	public void setContract( String contract )
	{
		this.contract = contract;
	}

	public String getLogin()
	{
		return login;
	}

	public void setLogin( String login )
	{
		this.login = login;
	}

	public String getNas()
	{
		return nas;
	}

	public void setNas( String nas )
	{
		this.nas = nas;
	}

	public String getError()
	{
		return error;
	}

	public void setError( String error )
	{
		this.error = error;
	}
}
