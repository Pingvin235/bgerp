package ru.bgcrm.plugin.bgbilling.proto.model.crm.call;

public class Call
{
	private String contract;
	private Integer contractId;
	private Integer id;
	private String problem;
	private String time;
	private String type;
	private String user;
	
	public String getContract()
	{
		return contract;
	}
	public void setContract( String contract )
	{
		this.contract = contract;
	}
	public Integer getContractId()
	{
		return contractId;
	}
	public void setContractId( Integer contractId )
	{
		this.contractId = contractId;
	}
	public Integer getId()
	{
		return id;
	}
	public void setId( Integer id )
	{
		this.id = id;
	}
	public String getProblem()
	{
		return problem;
	}
	public void setProblem( String problem )
	{
		this.problem = problem;
	}
	public String getTime()
	{
		return time;
	}
	public void setTime( String time )
	{
		this.time = time;
	}
	public String getType()
	{
		return type;
	}
	public void setType( String type )
	{
		this.type = type;
	}
	public String getUser()
	{
		return user;
	}
	public void setUser( String user )
	{
		this.user = user;
	}

}
